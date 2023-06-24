package persistencia;

import comun.dados.Conta;
import comun.dados.Estado;
import comun.dados.Login;
import comun.dados.Transacao;
import comun.protocolo.Protocolo;
import comun.protocolo.TipoProtocolo;
import comun.util.Constantes;
import comun.util.ParseJsonServico;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import java.io.Serializable;
import java.util.Objects;

public class Persistencia extends ReceiverAdapter implements RequestHandler, Serializable {

    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;
    private PersistenciaServico persistenciaServico;
    private Estado estado;
    private boolean sincronizando = false;

    public void inicia() throws Exception {
        persistenciaServico = new PersistenciaServico();

        canalDeComunicacao = new JChannel(Constantes.XML_TESTE);
        canalDeComunicacao.setReceiver(this);

        despachante = new MessageDispatcher(canalDeComunicacao, null, this, this);

        canalDeComunicacao.connect(Constantes.CANAL_PERSISTENCIA);
        eventLoop();
        canalDeComunicacao.close();
    }

    private void eventLoop() {

        try {
            Protocolo prot = new Protocolo();
            prot.setConteudo("teste");
            prot.setResposta(false);
            prot.setTipo(TipoProtocolo.TESTE_PERSISTENCIA);

            enviaMulticast(prot);

        } catch (Exception e) {
            System.err.println("ERRO: " + e.toString());
        }

        sincronizando = true;

        estado = new Estado();
        System.out.println(canalDeComunicacao.getView().getMembers().toString());
        if (canalDeComunicacao.getView().getMembers().size() > 1) {
            try {
                var protocolo = new Protocolo();
                protocolo.setTipo(TipoProtocolo.TESTE_VISAO);
                var estadoJson = enviaUnicast(canalDeComunicacao.getView().getMembers().get(0), protocolo);
                estado = ParseJsonServico.parseEstadoDeJson(estadoJson);
                gravarEstado();
            } catch (Exception e) {
                System.out.println("ERRO - Não foi possivel iniciar a Persistencia");
                System.exit(1);
            }
        }
        sincronizando = false;
        System.out.println("Persistencia Funcional!");
        while (true) {
            Util.sleep(100);
        }
    }

    private RspList enviaMulticast(Protocolo conteudo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + conteudo.getConteudo());

        Address cluster = null;
        Message mensagem = new Message(cluster, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)
        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //MULTICAST
        return respList;
    }

    private void gravarEstado() {
        persistenciaServico.salvaEstado(estado);
    }

    private String enviaUnicast(Address destino, Protocolo protocolo) throws Exception {
        Message mensagem = new Message(destino, protocolo);
        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST);
        Object resp = despachante.sendMessage(mensagem, opcoes);
        return (String) resp;
    }

    @Override
    public void viewAccepted(View new_view) {
    }

    @Override
    public Object handle(Message msg) throws Exception {
        Protocolo pergunta = (Protocolo) msg.getObject();

        while (sincronizando) {
            Util.sleep(100);
        }

        if (TipoProtocolo.PERSISTE_USUARIO.equals(pergunta.getTipo())) {
            var conta = ParseJsonServico.parseContaDeJson(pergunta.getConteudo());
            if (criarUsuario(conta)) {
                System.out.println("Usuario Cadastrado: " + pergunta.getConteudo());
                return ("y");
            }
            System.out.println("Usuario Indisponivel: " + pergunta.getConteudo());
            return ("n");
        }

        if (TipoProtocolo.BUSCA_LOGIN_DO_ARQUIVO.equals(pergunta.getTipo())) {
            var login = ParseJsonServico.parseLoginDeJson(pergunta.getConteudo());
            if (realizarLogin(login)) {
                System.out.println("Acesso Permitido: " + pergunta.getConteudo());
                return ("y");
            }
            System.out.println("Acesso Negado: " + pergunta.getConteudo());
            return ("n");
        }

        if (TipoProtocolo.PERSISTE_TRANSACAO.equals(pergunta.getTipo())) {
            var transacao = ParseJsonServico.parseTransacaoDeJson(pergunta.getConteudo());
            if (cadastrarTransacao(transacao)) {
                System.out.println("Transacao: " + pergunta.getConteudo());
                return("y");
            }
            System.out.println("ERRO");
            return("n");
        }

        return null;
    }

    private boolean realizarLogin(Login login) {
        var contaOpt = estado.getContas()
                .parallelStream()
                .filter(conta -> Objects.equals(conta.getIdentificador(), login.getConta()))
                .findFirst();

        if (contaOpt.isPresent()){
            var conta =  contaOpt.get();
            return Objects.equals(conta.getSenha(), login.getSenha());
        }
        return false;
    }

    private boolean criarUsuario(Conta novaConta) {
        var contemConta = estado.getContas()
                .parallelStream()
                .anyMatch(
                        conta -> Objects.equals(conta.getIdentificador(), novaConta.getIdentificador())
                );
        if (!contemConta){
            estado.getContas().add(novaConta);
            persistenciaServico.salvaEstado(estado);
            return true;
        }
        return false;
    }

    private boolean cadastrarTransacao(Transacao transacao) {
        var contaPagadoraOpt = estado.getContas()
                .parallelStream()
                .filter(
                        conta -> Objects.equals(conta.getIdentificador(), transacao.getIdentificadorPagador())
                ).findFirst();
        var contaRecebedoraOpt = estado.getContas()
                .parallelStream()
                .filter(
                        conta -> Objects.equals(conta.getIdentificador(), transacao.getIdentificadorRecebedor())
                ).findFirst();
        if (contaPagadoraOpt.isPresent() && contaRecebedoraOpt.isPresent()) {
            var contaPagadora = contaPagadoraOpt.get();
            var contaRecebedora = contaRecebedoraOpt.get();

            if (transacao.getValor().compareTo(contaPagadora.getSaldo()) <= 0) {
                contaPagadora.getTransacoes().add(transacao);
                contaRecebedora.getTransacoes().add(transacao);
                persistenciaServico.salvaEstado(estado);
                return true;
            }
        }
        return false;
    }
}
