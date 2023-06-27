package main.v2.camadas;

import comun.dados.Estado;
import comun.dados.Extrato;
import comun.dados.Resposta;
import comun.protocolo.Protocolo;
import comun.protocolo.TipoProtocolo;
import comun.util.Constantes;
import comun.util.ParseJsonServico;
import main.PersistenciaServico;
import main.v2.tipos.TipoCanal;
import main.v2.vservico.ServicoEnvio;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;

import java.io.InputStream;
import java.io.OutputStream;

public class Persistencia extends ReceiverAdapter implements RequestHandler {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;

    private String contaLogada;
    private Estado estado;

    private final PersistenciaServico persistenciaServico = new PersistenciaServico();

    public void inicia() throws Exception{
        canalDeComunicacao=new JChannel(Constantes.XML_TESTE);

        canalDeComunicacao.setReceiver(this);

        despachante = new MessageDispatcher(canalDeComunicacao, this, this, this);

        canalDeComunicacao.connect(TipoCanal.PERSISTENCIA.name());

//        todo verificar o set e get state
//        this.trataEstado();
    }

    public void finaliza(){
        canalDeComunicacao.close();
    }

    private void trataEstado() throws Exception {
        if (canalDeComunicacao.getView().getMembers().size() == 1){
            this.estado = persistenciaServico.recuperaEstado();
            return;
        }
        var envelope = new Protocolo("", TipoProtocolo.PERSISTENCIA_BUSCA_ESTADO);

        var protocoloResposta = ServicoEnvio.enviaMulticast(envelope, despachante).getResults().get(0);
        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());

        if (!resposta.isSucesso()){
            System.out.println("Impossível de buscar o estado, suba o processo novamente!");
            canalDeComunicacao.close();
            return;
        }
        var estado = ParseJsonServico.parseEstadoDeJson(resposta.getMensagem());
        this.estado = estado;
        persistenciaServico.salvaEstado(this.estado);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        output.write(ParseJsonServico.parseParaJson(this.estado).getBytes());
    }

    @Override
    public void setState(InputStream input) throws Exception {
        var estadoJson = new String(input.readAllBytes());
        this.estado = ParseJsonServico.parseEstadoDeJson(estadoJson);
    }

    @Override
    public Object handle(Message message) throws Exception {
        var protocolo = ParseJsonServico.parseProtocoloDeJson((String) message.getObject());

        switch (protocolo.getTipo()){
            case CONTROLE_LOGIN:
                protocolo.setConteudo(processaLogin(protocolo.getConteudo()));
                return protocolo;
            case CONTROLE_CRIACAO_CONTA:
                protocolo.setConteudo(processaCraicaoConta(protocolo.getConteudo()));
                return protocolo;
            case CONTROLE_TRANSFERENCIA:
                protocolo.setConteudo(processaTransferencia(protocolo.getConteudo()));
                return protocolo;
            case CONTROLE_EXTRATO:
                protocolo.setConteudo(processaExtrato(protocolo.getConteudo()));
                return protocolo;
            case PERSISTENCIA_BUSCA_ESTADO:
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
