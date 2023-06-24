package visao;

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
import org.jgroups.util.Util;
import persistencia.PersistenciaServico;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Visao extends ReceiverAdapter implements RequestHandler {

    private final Scanner scan;
    private JChannel canalDeComunicacao;
    private MessageDispatcher despachante;
    private String contaLogada;
    private Vector<Address> grupo;
    private String historico;
    private final PersistenciaServico persistenciaServico;

    public Visao(){
        this.scan = new Scanner(System.in);
        this.contaLogada = "";
        this.grupo = new Vector<>();
        this.historico = "[Historico]";

        this.persistenciaServico = new PersistenciaServico();
    }

    public void inicia() throws Exception{
        JChannel canalDeComunicacaoControle = new JChannel(Constantes.XML_TESTE);
        canalDeComunicacaoControle.connect(Constantes.CANAL_CONTROLE);
        canalDeComunicacaoControle.setReceiver(this);
        despachante = new MessageDispatcher(canalDeComunicacaoControle, null, this, this);

        contaLogada = canalDeComunicacaoControle.getName();

        controleMenuInicial();

        Protocolo protocoloSaudacao = new Protocolo();
        protocoloSaudacao.setConteudo(TipoProtocolo.TESTE_VISAO.name());
        protocoloSaudacao.setResposta(false);
        protocoloSaudacao.setTipo(TipoProtocolo.TESTE_VISAO);

        try {
            enviaMulticastNone(protocoloSaudacao);
        } catch (Exception e) {
            e.printStackTrace();
        }
        canalDeComunicacaoControle.close();

        canalDeComunicacao = new JChannel(Constantes.XML_TESTE);
        canalDeComunicacao.setName(contaLogada);
        canalDeComunicacao.setReceiver(this);
        despachante = new MessageDispatcher(canalDeComunicacao, null, this, this);

        canalDeComunicacao.connect(Constantes.CANAL_VISAO);

        eventLoop();
        canalDeComunicacao.close();
    }

    private void eventLoop() {
        var opcao = Integer.valueOf(1);

        while (opcao != 3){
            opcao = Menu.menuPrincipal();

            switch (opcao){
                case 1:
                    realizarTransferencia();
                    break;
                case 2:
                    //todo buscar saldo
                    break;
            }
        }
    }

    private void controleMenuInicial() {
        var opcao = Integer.valueOf(1);

        while (opcao != 3){
            opcao = Menu.menuLogin();

            switch (opcao){
                case 1:
                    var resultadoCriacao = this.criaConta();
                    if (resultadoCriacao){
                        opcao = 3;
                        criaArquivoContas();
                    }
                    break;
                case 2:
                    if (realizarLogin()) {
                        opcao = 3;
                        criaArquivoContas();
                    }
                    break;
            }
        }
    }

    private boolean realizarLogin() {
        boolean certo = false;
        try{
            JChannel canalDeComunicacaoControle = new JChannel(Constantes.XML_TESTE);
            canalDeComunicacaoControle.setName(contaLogada);
            canalDeComunicacaoControle.connect(Constantes.CANAL_CONTROLE);
            canalDeComunicacaoControle.setReceiver(this);
            despachante = new MessageDispatcher(canalDeComunicacaoControle, null, null, this);

            Protocolo protocoloLogin = new Protocolo();
            protocoloLogin.setResposta(false);
            protocoloLogin.setTipo(TipoProtocolo.LOGIN);

            var login = Menu.criaLogin();
            var jsonLogin = ParseJsonServico.parseParaJson(login);
            protocoloLogin.setConteudo(jsonLogin);

            String resp = enviaUnicast(canalDeComunicacaoControle.getView().getMembers().get(0), (protocoloLogin));

            if (resp.contains("y")) {
                System.out.println("Login efetuado com sucesso !");
                contaLogada = protocoloLogin.getConteudo();
                certo = true;
            } else {
                System.out.println("ERRO");
            }

            canalDeComunicacaoControle.close();
            despachante = new MessageDispatcher(canalDeComunicacao, null, null, this);

        } catch (Exception e){
            e.printStackTrace();
        }
        return certo;
    }

    private boolean criaConta() {
        var certo = false;
        try{
            JChannel canalDeComunicacaoControle = new JChannel(Constantes.XML_TESTE);
            canalDeComunicacaoControle.setName(contaLogada);
            canalDeComunicacaoControle.connect(Constantes.CANAL_CONTROLE);
            canalDeComunicacaoControle.setReceiver(this);
            despachante = new MessageDispatcher(canalDeComunicacaoControle, null, null, this);

            var protocoloCriaConta = new Protocolo();
            protocoloCriaConta.setResposta(false);
            protocoloCriaConta.setTipo(TipoProtocolo.CRIACAO_CONTA);

            var conta = Menu.criaConta();
            //todo chamada para o identificador

            String resp = enviaUnicast(canalDeComunicacaoControle.getView().getMembers().get(0), (protocoloCriaConta));

            if (resp.contains("y")){
                System.out.println("Conta cadastrada com sucesso!");
                contaLogada = protocoloCriaConta.getConteudo();
                certo = true;
            }else {
                System.out.println("Erro ao criar a conta!");
            }

            canalDeComunicacaoControle.close();
            despachante = new MessageDispatcher(canalDeComunicacao, null, null, this);
        }catch (Exception e){
            e.printStackTrace();
        }
        return certo;
    }

    private void criaArquivoContas() {
        File contaLogadaFile = new File("./files/contas.txt");
        try {
            BufferedWriter auxout = new BufferedWriter(new FileWriter(contaLogadaFile));
            auxout.append(contaLogada);
            auxout.close();
        } catch (Exception e) {
            System.out.println();
        }
    }


    private boolean realizarTransferencia() {
        boolean certo = false;
        try{
            JChannel canalDeComunicacaoControle = new JChannel(Constantes.XML_TESTE);
            canalDeComunicacaoControle.setName(contaLogada);
            canalDeComunicacaoControle.connect(Constantes.CANAL_CONTROLE);
            canalDeComunicacaoControle.setReceiver(this);
            despachante = new MessageDispatcher(canalDeComunicacaoControle, null, null, this);

            Protocolo protocoloTransferencia = new Protocolo();
            protocoloTransferencia.setResposta(false);
            protocoloTransferencia.setTipo(TipoProtocolo.TRANSFERENCIA);

            var transferencia = Menu.criaTransferencia(this.contaLogada);
            var json = ParseJsonServico.parseParaJson(transferencia);
            protocoloTransferencia.setConteudo(json);

            String resp = enviaUnicast(canalDeComunicacaoControle.getView().getMembers().get(0), (protocoloTransferencia));

            if (resp.contains("y")) {
                System.out.println("Transferencia efetuada!");
                certo = true;
            } else {
                System.out.println("ERRO");
            }

            canalDeComunicacaoControle.close();
            despachante = new MessageDispatcher(canalDeComunicacao, null, null, this);

        } catch (Exception e){
            e.printStackTrace();
        }
        return certo;
    }


    private String enviaUnicast(Address destino, Protocolo conteudo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + conteudo.getConteudo());

        var mensagem = new Message(destino, conteudo);

        var opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // não espera receber a resposta do destino (ALL, MAJORITY, FIRST, NONE)

        String resp = despachante.sendMessage(mensagem, opcoes); //UNICAST

        return resp;
    }

    private void enviaMulticastNone(Protocolo protocolo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + protocolo.getConteudo());

        Address cluster = null;
        Message mensagem = new Message(cluster, protocolo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_NONE);
        opcoes.setAnycasting(false);

        despachante.castMessage(null, mensagem, opcoes);
    }

    @Override
    public void receive(Message msg) {
        System.out.println("" + msg.getSrc() + ": " + msg.getObject());
    }

    @Override
    public Object handle(Message msg) throws Exception {
        // responde requisições recebidas
        Protocolo pergunta = (Protocolo) msg.getObject();

        var toIgnore = List.of(
                TipoProtocolo.LOGIN,
                TipoProtocolo.TRANSFERENCIA,
                TipoProtocolo.TIPO_15,
                TipoProtocolo.TIPO_16,
                TipoProtocolo.TIPO_30
        );

        if (toIgnore.contains(pergunta.getTipo())) {
            Util.sleep(1000);
            return null;
        }

        //Olhar se o msg.src pertence a algum grupo.
        if (TipoProtocolo.TIPO_6.equals(pergunta.getTipo())) {
            System.out.println(msg.src());
            System.out.println(grupo.get(0));

            for (Address address : grupo) {
                if (address.toString().equals(msg.src().toString())) {
                    System.out.println("Encontrei historico");
                    System.out.println(historico);
                    return historico;
                }
            }
            Util.sleep(1000);
            return "Nao posso historico.";
        }

        if (TipoProtocolo.TESTE_PERSISTENCIA.equals(pergunta.getTipo())) {
            System.out.println("Lance : " + pergunta.getConteudo() + "\n");
        } else {
            System.out.println("Recebi uma msg : " + pergunta.getConteudo() + "\n");
        }

        String line = "";
        Protocolo prot = new Protocolo();

        if (TipoProtocolo.TIPO_3.equals(pergunta.getTipo())) {
            return "n";
        }

        return prot;
    }
}
