import dados.Conta;
import dados.Estado;
import dados.Transacao;
import servicos.ParseJsonServico;
import util.Constantes;

import java.math.BigDecimal;
import java.util.List;

public class TesteSalvandoEstado {

    public static void main(String[] args) {
        var transacao1 = new Transacao();
        transacao1.setIdentificadorPagador(1L);
        transacao1.setIdentificadorRecebedor(2L);
        transacao1.setValor(BigDecimal.valueOf(Constantes.VALOR_SALDO_INICIAL));
        var transacao2 = new Transacao();
        transacao2.setIdentificadorPagador(2L);
        transacao2.setIdentificadorRecebedor(1L);
        transacao2.setValor(BigDecimal.valueOf(Constantes.VALOR_SALDO_INICIAL));

        var transacoes = List.of(transacao1, transacao2);

        var conta = new Conta();
        conta.setIdentificador(1L);
        conta.setSenha("SENHA");
        conta.setNomeCliente("CLIENTEeeeeeee");
        conta.setTransacoes(transacoes);

        var contas = List.of(conta);

        var estado = new Estado();
        estado.setContas(contas);

        System.out.println(ParseJsonServico.parseParaJson(estado));
    }
}
