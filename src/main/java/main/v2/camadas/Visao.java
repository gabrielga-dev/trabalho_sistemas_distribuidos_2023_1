package main.v2.camadas;

import comun.protocolo.Protocolo;
import comun.protocolo.TipoProtocolo;
import comun.util.Constantes;
import comun.util.ParseJsonServico;
import main.Menu;
import main.v2.tipos.TipoCanal;
import main.v2.vservico.ServicoEnvio;
import org.jgroups.JChannel;
import org.jgroups.blocks.MessageDispatcher;

public class Visao {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;
    private String contaLogada;

    public void inicia() throws Exception {
        canalDeComunicacao=new JChannel(Constantes.XML_TESTE);

        despachante = new MessageDispatcher(canalDeComunicacao, null);

        canalDeComunicacao.connect(TipoCanal.CONTROLE.name());

        eventLoop();
        canalDeComunicacao.close();
    }

    private void eventLoop() throws Exception {
        var opcao = Integer.valueOf(1);

        while (opcao != 3) {
            opcao = Menu.menuLogin();

            switch (opcao) {
                case 1:
                    this.criaConta();
                    break;
                case 2:
                    if (realizarLogin()) {
                        eventLoopLogged();
                    }
                    break;
                default:
                    opcao = 3;
                    contaLogada = null;
            }
        }
    }

    private void eventLoopLogged() throws Exception {
        var opcao = Integer.valueOf(1);

        while (true) {
            opcao = Menu.menuPrincipal();

            switch (opcao) {
                case 1:
                    this.transferir();
                    break;
                case 2:
                    this.emitirExtrato();
                    break;
                case 3:
                    this.contaLogada = null;
                    return;
            }
        }
    }

    private void criaConta() throws Exception {
        var conta = Menu.criaConta();
        var contaJson = ParseJsonServico.parseParaJson(conta);
        var envelope = new Protocolo(contaJson, TipoProtocolo.VISAO_CRIACAO_CONTA);

        var protocoloResposta = ServicoEnvio.enviaMulticast(envelope, despachante).getResults().get(0);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (resposta.isSucesso()){
            System.out.println("Conta criada com sucesso! Identificador: conta: " + conta.getIdentificador());
        } else {
            System.out.println("ERRO! " + resposta.getMensagem());
        }
    }

    private boolean realizarLogin() throws Exception {
        var login = Menu.criaLogin();
        var loginJson = ParseJsonServico.parseParaJson(login);
        var envelope = new Protocolo(loginJson, TipoProtocolo.VISAO_LOGIN);

        var protocoloResposta = ServicoEnvio.enviaUnicast(envelope, despachante, canalDeComunicacao);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (resposta.isSucesso()){
            System.out.println("Conta logada com sucesso! Identificador: conta: " + login.getConta());
            this.contaLogada = login.getConta();
            return true;
        } else {
            System.out.println("ERRO! " + resposta.getMensagem());
            return false;
        }
    }

    private void transferir() throws Exception {
        var transferencia = Menu.criaTransferencia(this.contaLogada);
        var transferenciaJson = ParseJsonServico.parseParaJson(transferencia);
        var envelope = new Protocolo(transferenciaJson, TipoProtocolo.VISAO_TRANSFERENCIA);

        var protocoloResposta = ServicoEnvio.enviaMulticast(envelope, despachante).getResults().get(0);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (resposta.isSucesso()){
            System.out.println("Transferido R$ " + transferencia.getValor() + " para a conta " + transferencia.getIdentificadorRecebedor());
        } else {
            System.out.println("ERRO! " + resposta.getMensagem());
        }
    }

    private void emitirExtrato() throws Exception {
        var envelope = new Protocolo(this.contaLogada, TipoProtocolo.VISAO_EXTRATO);

        var protocoloResposta = ServicoEnvio.enviaUnicast(envelope, despachante, canalDeComunicacao);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());
        if (!resposta.isSucesso()){
            System.out.println("ERRO! " + resposta.getMensagem());
            return;
        }
        var extrato = ParseJsonServico.parseExtratoDeJson(resposta.getMensagem());
        System.out.println("Saldo: R$" + extrato.getValorAtual().toString());
        extrato.getTransacoes()
                .forEach(
                        transacao -> {
                            System.out.println("Pagador: " + transacao.getIdentificadorPagador());
                            System.out.println("Recebedor: " + transacao.getIdentificadorRecebedor());
                            System.out.println("Valor: R$" + transacao.getValor().toString());
                            System.out.println("-----------------------------------------------------------");
                        }
                );
        System.out.println("\nExtrato da conta " + contaLogada);
    }
}
