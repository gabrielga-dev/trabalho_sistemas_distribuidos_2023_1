package servicos;

import com.google.gson.Gson;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ParseJsonServico {

    public static <T> String parseParaJson(T objeto){
        var gson = new Gson();
        return gson.toJson(objeto);
    }

}
