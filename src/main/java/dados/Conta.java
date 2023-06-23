package dados;

import lombok.Getter;
import lombok.Setter;
import util.Constantes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Conta implements Serializable {

    private String nomeCliente;
    private Long identificador;
    private String senha;

    private BigDecimal saldo;
    private List<Transacao> transacoes;

    public Conta(){
        transacoes = new ArrayList<>();
        saldo = BigDecimal.valueOf(Constantes.VALOR_SALDO_INICIAL);
    }
}
