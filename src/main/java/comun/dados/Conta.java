package comun.dados;

import comun.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Conta implements Serializable {

    private String nomeCliente;
    private String identificador;
    private String senha;

    private List<Transacao> transacoes;

    public Conta(){
        var transacaoInicial = new Transacao();
        transacaoInicial.setValor(Constantes.VALOR_SALDO_INICIAL);
        transacaoInicial.setIdentificadorRecebedor(this.identificador);
        transacaoInicial.setIdentificadorPagador(null);

        transacoes = new ArrayList<>();
        transacoes.add(transacaoInicial);
    }

    public Long getSaldo(){
        var soma = 0L;
        var valores = this.transacoes
                .stream()
                .map(
                        transacao -> {
                            if (identificador.equals(transacao.getIdentificadorPagador())){
                                return transacao.getValor() * (-1);
                            }
                            return transacao.getValor();
                        }
                ).collect(Collectors.toList());
        for (Long valor : valores) {
            soma = soma + valor;
        }
        return soma;
    }

    public boolean validacao() {
        return (nomeCliente != null) && (identificador != null) && (senha != null) && (transacoes != null) &&
                (!nomeCliente.isBlank()) && (!identificador.isBlank()) && (!senha.isBlank()) &&
                (transacoes.size() == 1) && (transacoes.get(0).getIdentificadorPagador() == null);
    }
}
