package comun.dados;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Transacao implements Serializable {

    private String identificadorPagador;
    private String identificadorRecebedor;
    private Long valor;

    public boolean validacao() {
        return (identificadorPagador != null) && (identificadorRecebedor != null) && (valor != null) && (valor > 0) &&
                (!identificadorRecebedor.equals(identificadorPagador));
    }
}
