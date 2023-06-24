package comun.dados;

import lombok.Getter;
import lombok.Setter;
import comun.util.Constantes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Conta implements Serializable {

    private String nomeCliente;
    private String identificador;
    private String senha;

    private List<Transacao> transacoes;

    public Conta(){
        var transacaoInicial = new Transacao();
        transacaoInicial.setValor(BigDecimal.valueOf(Constantes.VALOR_SALDO_INICIAL));
        transacaoInicial.setIdentificadorRecebedor(this.identificador);
        transacaoInicial.setIdentificadorPagador(null);

        transacoes = new ArrayList<>();
        transacoes.add(transacaoInicial);
    }

    public BigDecimal getSaldo(){
        var soma = BigDecimal.ZERO;
        var valores = new ArrayList<BigDecimal>();
        this.transacoes
                .stream()
                .map(
                        transacao -> {
                            if ((identificador.compareTo(transacao.getIdentificadorPagador())) == 0){
                                return transacao.getValor().negate();
                            }
                            return transacao.getValor();
                        }
                ).forEach(valores::add);
        for (BigDecimal valor : valores) {
            soma = soma.add(valor);
        }
        return soma;
    }
}
