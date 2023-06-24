import comun.dados.Conta;
import comun.dados.Estado;
import comun.dados.Transacao;
import comun.util.ParseJsonServico;
import comun.util.Constantes;

import java.math.BigDecimal;
import java.util.List;

public class TesteSalvandoEstado {

    public static void main(String[] args) {
        var transacao1 = new Transacao();
        transacao1.setIdentificadorPagador("1");
        transacao1.setIdentificadorRecebedor("2");
        transacao1.setValor(BigDecimal.valueOf(10l));
        var transacao2 = new Transacao();
        transacao2.setIdentificadorPagador("2");
        transacao2.setIdentificadorRecebedor("1");
        transacao2.setValor(BigDecimal.valueOf(100l));

        var transacoes = List.of(transacao1, transacao2);

        var conta = new Conta();
        conta.setIdentificador("1");
        conta.setSenha("SENHA");
        conta.setNomeCliente("CLIENTEeeeeeee");
        conta.setTransacoes(transacoes);

        var contas = List.of(conta);

        var estado = new Estado();
        estado.setContas(contas);

        System.out.println(ParseJsonServico.parseParaJson(estado));
        System.out.println(conta.getSaldo());
    }
}
