import comun.util.ParseJsonServico;
import persistencia.PersistenciaServico;

import java.math.BigDecimal;

public class TesteRecuperandoEstado {

    public static void main(String[] args) throws Exception {
        System.out.println(BigDecimal.ONE);
        System.out.println(BigDecimal.ONE.negate());
        var servico = new PersistenciaServico();

        var estado = servico.recuperaEstado();

        System.out.println(ParseJsonServico.parseParaJson(estado));
    }
}
