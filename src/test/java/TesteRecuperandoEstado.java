import servicos.ParseJsonServico;
import servicos.PersistenciaServico;

public class TesteRecuperandoEstado {

    public static void main(String[] args) {
        var servico = new PersistenciaServico();

        var estado = servico.recuperaEstado();

        System.out.println(ParseJsonServico.parseParaJson(estado));
    }
}
