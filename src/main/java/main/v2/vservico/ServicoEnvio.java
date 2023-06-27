package main.v2.vservico;

import comun.protocolo.Protocolo;
import comun.util.ParseJsonServico;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServicoEnvio {

    public static RspList<Protocolo> enviaMulticast(
            Protocolo envelope, MessageDispatcher despachante
    ) throws Exception {
        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster

        var jsonEnvelope = ParseJsonServico.parseParaJson(envelope);
        Message mensagem=new Message(cluster, jsonEnvelope);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_MAJORITY); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        return despachante.castMessage(null, mensagem, opcoes);
    }

    public static Protocolo enviaUnicast(
            Protocolo envelope, MessageDispatcher despachante, JChannel canalDeComunicacao
    ) throws Exception {
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
}
