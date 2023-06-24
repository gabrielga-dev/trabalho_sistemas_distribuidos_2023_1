package comun.dados;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class Transacao implements Serializable {

    private String identificadorPagador;
    private String identificadorRecebedor;
    private BigDecimal valor;

    private String server;
}
