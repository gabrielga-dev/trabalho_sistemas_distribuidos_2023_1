package controle;

import comun.dados.Estado;
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
import org.jgroups.util.Util;

import java.io.Serializable;
import java.util.Vector;

public class Controle extends ReceiverAdapter implements RequestHandler, Serializable {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;

    private boolean controle = true;
    private boolean sincronizando = false;

    private Estado estado;

    public void inicia() throws Exception {
        canalDeComunicacao = new JChannel(Constantes.XML_TESTE);

        canalDeComunicacao.setReceiver(this);
        despachante = new MessageDispatcher(canalDeComunicacao, null, this, this);

        canalDeComunicacao.connect(Constantes.CANAL_CONTROLE);

        eventLoop();
        canalDeComunicacao.close();
    }

    private void eventLoop() {
        sincronizando = true;
        this.estado = new Estado();
        System.out.println(canalDeComunicacao.getView().getMembers().toString());
        if (canalDeComunicacao.getView().getMembers().size() > 1) {
            try {
                var protocolo = new Protocolo();
                protocolo.setTipo(TipoProtocolo.TIPO_90);
                this.estado = enviaUnicastSincronia(canalDeComunicacao.getView().getMembers().get(0), protocolo);
            } catch (Exception e) {
                System.out.println("ERRO - Não foi possivel iniciar o Controle");
                System.exit(1);
            }
        }
        sincronizando = false;
        System.out.println("Controle funcionando !");


        var protocolo = new Protocolo();
        protocolo.setConteudo("Teste-Controle");
        protocolo.setResposta(false);
        protocolo.setTipo(TipoProtocolo.TESTE_CONTROLE);
        try {
            enviaMulticastnNone(protocolo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Address meuEndereco = canalDeComunicacao.getAddress();

        while (true) {
            Vector<Address> cluster = new Vector<Address>(canalDeComunicacao.getView().getMembers());
            Address primeiroMembro = cluster.elementAt(0);  //0 a N
            if (meuEndereco.equals(canalDeComunicacao.getView().getMembers().get(0))) {
                System.out.println("Eu sou o primeiro.");
            }
            Util.sleep(10000);
        }
    }

    private Estado enviaUnicastSincronia(Address destino, Protocolo protocolo) throws Exception {
        Message mensagem = new Message(destino, protocolo);
        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST);
        var resp = (String) despachante.sendMessage(mensagem, opcoes);
        return ParseJsonServico.parseEstadoDeJson(resp);
    }

    private void enviaMulticastnNone(Protocolo conteudo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + conteudo.getConteudo());

        Address cluster = null;
        Message mensagem = new Message(cluster, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_NONE);
        opcoes.setAnycasting(false);

        despachante = new MessageDispatcher(canalDeComunicacao, null, this, this);
        despachante.castMessage(null, mensagem, opcoes);
    }

    public void receive(Message msg) {
        System.out.println("" + msg.getSrc() + ": " + msg.getObject());
    }

    public Object handle(Message msg) throws Exception {
        Protocolo pergunta = (Protocolo) msg.getObject();

        while (sincronizando) {
            Util.sleep(100);
        }

        if (TipoProtocolo.CRIACAO_CONTA.equals(pergunta.getTipo())) {
            System.out.println("Nova conta " + pergunta.getConteudo());
            pergunta.setTipo(TipoProtocolo.PERSISTE_USUARIO);
            if (cadastrarLogarUsuario(pergunta)) //todo mudar para apenas cadastrar
                return "y";
            else
                return "n";
        }

        if (TipoProtocolo.LOGIN.equals(pergunta.getTipo())) {
            System.out.println("Logar conta " + pergunta.getConteudo());
            pergunta.setTipo(TipoProtocolo.BUSCA_LOGIN_DO_ARQUIVO);
            if (cadastrarLogarUsuario(pergunta)) //todo mudar para apenas logar
                return "y";
            else
                return "n";
        }

        if (TipoProtocolo.TRANSFERENCIA.equals(pergunta.getTipo())) {
            System.out.println("Produto: " + pergunta.getConteudo());
            pergunta.setTipo(TipoProtocolo.PERSISTE_TRANSACAO);
            if (cadastrarLogarUsuario(pergunta)) //todo mudar para apenas cadastrar o a transacao
                return "y";
            else
                return "n";
        }

        Util.sleep(1000);

        return null;
    }

    private boolean cadastrarLogarUsuario(Protocolo protocolo) {
        boolean resp = false;

        try {
            JChannel canalDeComunicacaoControle = new JChannel(Constantes.XML_TESTE);
            canalDeComunicacaoControle.connect(Constantes.CANAL_PERSISTENCIA);
            canalDeComunicacaoControle.setReceiver(this);
            MessageDispatcher despachante0 = new MessageDispatcher(canalDeComunicacaoControle, null, null, this);

            String resposta = enviaMulticastFirst(protocolo, despachante0).getFirst().toString();

            if (resposta.contains("y")) {
                resp = true;
            }

            canalDeComunicacaoControle.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return resp;
    }

    private RspList enviaMulticastFirst(Protocolo conteudo, MessageDispatcher  despachante1) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo.getConteudo());

        Address cluster = null;
        Message mensagem=new Message(cluster, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST);
        opcoes.setAnycasting(false);

        RspList respList = despachante1.castMessage(null, mensagem, opcoes);
        return respList;
    }
}
