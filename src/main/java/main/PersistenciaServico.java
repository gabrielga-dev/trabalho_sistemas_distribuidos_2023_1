package main;

import comun.dados.Estado;
import comun.util.ParseJsonServico;
import lombok.SneakyThrows;
import comun.util.Constantes;

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

    public Estado recuperaEstado() throws Exception {
        // Verificar se o arquivo existe
        File arquivo = new File(Constantes.ARQUIVO_ESTADO_PATH);
        if (!arquivo.exists()) {
            criaArquivoSeNaoExistir();
            var estado = new Estado();
            salvaEstado(estado);
            return estado;
        }

        // Ler o conteúdo do arquivo JSON
        StringBuilder json = new StringBuilder();
        try (FileReader reader = new FileReader(arquivo)) {
            int character;
            while ((character = reader.read()) != -1) {
                json.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ParseJsonServico.parseEstadoDeJson(json.toString());
    }
}
