package servicos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dados.Estado;
import lombok.SneakyThrows;
import util.Constantes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PersistenciaServico {

    private File criaArquivoSeNaoExistir() {
        File arquivo = new File(Constantes.ARQUIVO_ESTADO_PATH);

        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return arquivo;
    }

    @SneakyThrows
    public void salvaEstado(Estado estado){
        var arquivo = criaArquivoSeNaoExistir();

        try (FileWriter writer = new FileWriter(arquivo)) {
            var json = ParseJsonServico.parseParaJson(estado);
            writer.write(json);
            System.out.println("Objeto salvo com sucesso no arquivo JSON!");
        } catch (IOException e) {
            throw e;
        }
    }

    public Estado recuperaEstado(){
        // Verificar se o arquivo existe
        File arquivo = new File(Constantes.ARQUIVO_ESTADO_PATH);
        if (!arquivo.exists()) {
            System.out.println("O arquivo JSON não existe.");
            throw new RuntimeException("Não existe arquivo de estado!");
        }

        // Ler o conteúdo do arquivo JSON
        String json = "";
        try (FileReader reader = new FileReader(arquivo)) {
            int character;
            while ((character = reader.read()) != -1) {
                json += (char) character;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        var gson = new Gson();
        try {
            return gson.fromJson(json, Estado.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
    }
}
