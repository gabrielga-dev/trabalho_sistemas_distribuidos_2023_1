package comun.dados;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Extrato implements Serializable {

    private Long valorAtual;
    private List<Transacao> transacoes;
}
