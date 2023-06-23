package servicos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dados.Estado;
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

}
