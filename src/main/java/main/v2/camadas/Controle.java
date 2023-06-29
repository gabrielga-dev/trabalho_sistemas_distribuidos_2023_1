package main.v2.camadas;

import comun.dados.Resposta;
import comun.protocolo.Protocolo;
import comun.protocolo.TipoProtocolo;
import comun.util.Constantes;
import comun.util.ParseJsonServico;
import main.v2.tipos.TipoCanal;
import main.v2.vservico.ServicoEnvio;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;

import java.util.List;

public class Controle extends ReceiverAdapter implements RequestHandler {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;

    public void inicia() throws Exception{
        canalDeComunicacao=new JChannel(Constantes.XML_TESTE);

        canalDeComunicacao.setReceiver(this);

        despachante = new MessageDispatcher(canalDeComunicacao, this, this, this);

        canalDeComunicacao.connect(TipoCanal.CONTROLE.name());
    }

    public void finaliza(){
        canalDeComunicacao.close();
    }

    public void receive(Message msg) {
        System.out.println("" + msg.getSrc() + ": " + msg.getObject());
    }

    public Object handle(Message message) throws Exception {
        var protocolo = ParseJsonServico.parseProtocoloDeJson((String) message.getObject());

        switch (protocolo.getTipo()){
            case VISAO_LOGIN:
                protocolo.setConteudo(processaLogin(protocolo.getConteudo()));
                return protocolo;
            case VISAO_CRIACAO_CONTA:
                protocolo.setConteudo(processaCraicaoConta(protocolo.getConteudo()));
                return protocolo;
            case VISAO_TRANSFERENCIA:
                protocolo.setConteudo(processaTransferencia(protocolo.getConteudo()));
                return protocolo;
            case VISAO_EXTRATO:
                protocolo.setConteudo(processaExtrato(protocolo.getConteudo()));
                return protocolo;
            default:
                System.out.println("Recebida uma mensagem estranha: " + protocolo.getTipo());
        }
        return null;
    }

    private String processaLogin(String loginJson) throws Exception {
        var login = ParseJsonServico.parseLoginDeJson(loginJson);

        if (!login.validacao()){
            return ParseJsonServico.parseParaJson(
                    new Resposta(false, "Informações inválidas!")
            );
        }

        var envelope = new Protocolo(loginJson, TipoProtocolo.CONTROLE_LOGIN);

        var conexao = conectaCanalPersistencia();
        var despachante = (MessageDispatcher) conexao.get(1);
        var canalDeComunicacao = (JChannel) conexao.get(1);

        var protocoloResposta = ServicoEnvio.enviaUnicast(envelope, despachante, canalDeComunicacao);

        canalDeComunicacao.close();

        return protocoloResposta.getConteudo();
    }

    private String processaCraicaoConta(String contaJson) throws Exception {
        var conta = ParseJsonServico.parseContaDeJson(contaJson);
        if (!conta.validacao()){
            return ParseJsonServico.parseParaJson(
                    new Resposta(false, "Informações inválidas!")
            );
        }

        var conexao = conectaCanalPersistencia();
        var despachante = (MessageDispatcher) conexao.get(1);
        var canalDeComunicacao = (JChannel) conexao.get(1);
        var envelope = new Protocolo(contaJson, TipoProtocolo.CONTROLE_CRIACAO_CONTA);

        var protocoloResposta = ServicoEnvio.enviaMulticast(envelope, despachante).getResults().get(0);

        canalDeComunicacao.close();

        return protocoloResposta.getConteudo();
    }

    private String processaTransferencia(String transacaoJson) throws Exception {
        var transferencia = ParseJsonServico.parseTransacaoDeJson(transacaoJson);

        if(!transferencia.validacao()){
            return ParseJsonServico.parseParaJson(
                    new Resposta(false, "Informações inválidas!")
            );
        }
        var transferenciaJson = ParseJsonServico.parseParaJson(transferencia);
        var envelope = new Protocolo(transferenciaJson, TipoProtocolo.CONTROLE_TRANSFERENCIA);

        var conexao = conectaCanalPersistencia();
        var despachante = (MessageDispatcher) conexao.get(1);
        var canalDeComunicacao = (JChannel) conexao.get(1);

        var protocoloResposta = ServicoEnvio.enviaMulticast(envelope, despachante).getResults().get(0);

        canalDeComunicacao.close();

        return protocoloResposta.getConteudo();
    }

    private String processaExtrato(String identificadorConta) throws Exception {

        if((identificadorConta == null) || (identificadorConta.isBlank())){
            return ParseJsonServico.parseParaJson(
                    new Resposta(false, "Informações inválidas!")
            );
        }
        var envelope = new Protocolo(identificadorConta, TipoProtocolo.CONTROLE_EXTRATO);

        var conexao = conectaCanalPersistencia();
        var despachante = (MessageDispatcher) conexao.get(1);
        var canalDeComunicacao = (JChannel) conexao.get(1);

        var protocoloResposta = ServicoEnvio.enviaUnicast(envelope, despachante, canalDeComunicacao);

        canalDeComunicacao.close();

        var resposta = ParseJsonServico.parseRespostaDeJson(protocoloResposta.getConteudo());
        return  resposta.getMensagem();
    }

    private List<Object> conectaCanalPersistencia() throws Exception {

        JChannel canalPersistencia =new JChannel(Constantes.XML_TESTE);

        MessageDispatcher despachantePersistencia = new MessageDispatcher(canalPersistencia, null);

        canalPersistencia.connect(TipoCanal.PERSISTENCIA.name());

        return List.of(canalPersistencia, despachantePersistencia);
    }
}
