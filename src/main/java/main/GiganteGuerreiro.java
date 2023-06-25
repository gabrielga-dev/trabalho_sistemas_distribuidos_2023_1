package main;

import comun.dados.Estado;
import comun.dados.Extrato;
import comun.dados.Resposta;
import comun.protocolo.Protocolo;
import comun.protocolo.TipoProtocolo;
import comun.util.Constantes;
import comun.util.ParseJsonServico;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;

public class GiganteGuerreiro extends ReceiverAdapter implements RequestHandler {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;

    private String contaLogada;
    private Estado estado;

    private final PersistenciaServico persistenciaServico = new PersistenciaServico();


    public void inicia() throws Exception {
        canalDeComunicacao=new JChannel(Constantes.XML_TESTE);

        despachante = new MessageDispatcher(canalDeComunicacao, this, this, this);

        canalDeComunicacao.setReceiver(this);	//quem irá lidar com as mensagens recebidas

        canalDeComunicacao.connect("TRABALHO_SD");

        this.trataEstado();

        eventLoop();
        canalDeComunicacao.close();
    }

    private void trataEstado() throws Exception {
        if (canalDeComunicacao.getView().getMembers().size() == 1){
            this.estado = persistenciaServico.recuperaEstado();
            return;
        }
        var envelope = new Protocolo("", TipoProtocolo.BUSCA_ESTADO);

        var protocoloResposta = enviaMulticast(envelope).getResults().get(0);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (!resposta.isSucesso()){
            System.out.println("Impossível de buscar o estado, suba o processo novamente!");
            return;
        }
        var estado = ParseJsonServico.parseEstadoDeJson(resposta.getMensagem());
        this.estado = estado;
        persistenciaServico.salvaEstado(this.estado);
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
        var envelope = new Protocolo(contaJson, TipoProtocolo.CRIACAO_CONTA);

        var protocoloResposta = enviaMulticast(envelope).getResults().get(0);
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
        var envelope = new Protocolo(loginJson, TipoProtocolo.LOGIN);

        var protocoloResposta = enviaUnicast(envelope);
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
        var envelope = new Protocolo(transferenciaJson, TipoProtocolo.TRANSFERENCIA);

        var protocoloResposta = enviaMulticast(envelope).getResults().get(0);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (resposta.isSucesso()){
            System.out.println("Transferido R$ " + transferencia.getValor() + " para a conta " + transferencia.getIdentificadorRecebedor());
        } else {
            System.out.println("ERRO! " + resposta.getMensagem());
        }
    }

    private void emitirExtrato() throws Exception {
        var envelope = new Protocolo(this.contaLogada, TipoProtocolo.EXTRATO);

        var protocoloResposta = enviaUnicast(envelope);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());
        if (!resposta.isSucesso()){
            System.out.println("ERRO! " + resposta.getMensagem());
            return;
        }
        var extrato = ParseJsonServico.parseExtratoDeJson(resposta.getMensagem());
        System.out.println("\nExtrato da conta " + contaLogada);
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
    }

    private Protocolo enviaUnicast(Protocolo envelope) throws Exception {
        //System.out.println("\nENVIEI a pergunta: " + conteudo);

        var sorteado = 0 + Math.random() * (canalDeComunicacao.getView().getMembers().size());
        var servidorSorteado = canalDeComunicacao.getView().getMembers().get(Double.valueOf(sorteado).intValue());

        var jsonEnvelope = ParseJsonServico.parseParaJson(envelope);
        Message mensagem=new Message(servidorSorteado, jsonEnvelope);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // ESPERA receber a resposta do destino // Outras opções: MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        return despachante.sendMessage(mensagem, opcoes); //envia o UNICAST
    }

    private RspList<Protocolo> enviaMulticast(Protocolo envelope) throws Exception {
        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster

        var jsonEnvelope = ParseJsonServico.parseParaJson(envelope);
        Message mensagem=new Message(cluster, jsonEnvelope);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_MAJORITY); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        return despachante.castMessage(null, mensagem, opcoes);
    }


    @Override
    public Object handle(Message message) throws Exception {
        var protocolo = ParseJsonServico.parseProtocoloDeJson((String) message.getObject());

        switch (protocolo.getTipo()){
            case LOGIN:
                protocolo.setConteudo(processaLogin(protocolo.getConteudo()));
                return protocolo;
            case CRIACAO_CONTA:
                protocolo.setConteudo(processaCraicaoConta(protocolo.getConteudo()));
                return protocolo;
            case TRANSFERENCIA:
                protocolo.setConteudo(processaTransferencia(protocolo.getConteudo()));
                return protocolo;
            case EXTRATO:
                protocolo.setConteudo(processaExtrato(protocolo.getConteudo()));
                return protocolo;
            case BUSCA_ESTADO:
                protocolo.setConteudo(processaBuscaEstado());
                return protocolo;
        }
        return null;
    }

    private String processaLogin(String loginJson) {
        var login = ParseJsonServico.parseLoginDeJson(loginJson);

        var contaOpt = estado.getContas().stream().filter(
                conta -> conta.getIdentificador().equals(login.getConta()) &&
                        conta.getSenha().equals(login.getSenha())
        ).findFirst();

        if (contaOpt.isEmpty()){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Credenciais nãoe ncontradas!"));
        }
        return ParseJsonServico.parseParaJson(new Resposta(true));
    }

    private String processaTransferencia(String transacaoJson) {
        var transferencia = ParseJsonServico.parseTransacaoDeJson(transacaoJson);

        var contaPagadoraOpt = estado.getContas().stream().filter(
                conta -> conta.getIdentificador().equals(transferencia.getIdentificadorPagador())
        ).findFirst();
        var contaRecebedoraOpt = estado.getContas().stream().filter(
                conta -> conta.getIdentificador().equals(transferencia.getIdentificadorRecebedor())
        ).findFirst();

        if (contaPagadoraOpt.isEmpty()){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Conta pagadora não encontrada"));
        } else if(contaRecebedoraOpt.isEmpty()){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Conta recebedora não encontrada"));
        }

        var pagador = contaPagadoraOpt.get();
        var recebedor = contaRecebedoraOpt.get();

        if (pagador.getSaldo() < transferencia.getValor()){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Saldo insuficiente!"));
        }

        pagador.getTransacoes().add(transferencia);
        recebedor.getTransacoes().add(transferencia);
        persistenciaServico.salvaEstado(estado);

        return ParseJsonServico.parseParaJson(new Resposta(true));
    }

    private String processaCraicaoConta(String contaJson) {
        var conta = ParseJsonServico.parseContaDeJson(contaJson);
        var identificadorRepetido = estado.getContas()
                .stream()
                .anyMatch(
                        contaSalva -> contaSalva.getIdentificador().equals(conta.getIdentificador())
                );
        if (identificadorRepetido){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Identificador repetido!"));
        }
        estado.getContas().add(conta);
        persistenciaServico.salvaEstado(estado);
        return ParseJsonServico.parseParaJson(new Resposta(true, null));
    }

    private String processaExtrato(String identificadorConta) {
        var contaOpt = estado.getContas().stream().filter(
                conta -> conta.getIdentificador().equals(identificadorConta)
        ).findFirst();

        if (contaOpt.isEmpty()){
            return ParseJsonServico.parseParaJson(new Resposta(false, "Conta não encontrada"));
        }

        var conta = contaOpt.get();
        var extrato = new Extrato(conta.getSaldo(), conta.getTransacoes());
        var resposta = new Resposta(true, ParseJsonServico.parseParaJson(extrato));
        return ParseJsonServico.parseParaJson(resposta);
    }

    private String processaBuscaEstado() {
        var estadoJson = ParseJsonServico.parseParaJson(this.estado);
        var resposta = new Resposta(true, estadoJson);
        return ParseJsonServico.parseParaJson(resposta);
    }
}
