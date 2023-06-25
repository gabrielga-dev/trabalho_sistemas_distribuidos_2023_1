package comun.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import comun.dados.Conta;
import comun.dados.Estado;
import comun.dados.Extrato;
import comun.dados.Login;
import comun.dados.Resposta;
import comun.dados.Transacao;
import comun.protocolo.Protocolo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ParseJsonServico {

    public static <T> String parseParaJson(T objeto){
        var gson = new Gson();
        return gson.toJson(objeto);
    }


    public static Estado parseEstadoDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Estado.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Conta parseContaDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Conta.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Login parseLoginDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Login.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Transacao parseTransacaoDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Transacao.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Protocolo parseProtocoloDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Protocolo.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Resposta parseRespostaDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Resposta.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

    public static Extrato parseExtratoDeJson(String json){
        var gson = new Gson();
        try {
            return gson.fromJson(json, Extrato.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }

}
