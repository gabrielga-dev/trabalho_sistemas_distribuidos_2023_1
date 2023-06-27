package comun.dados;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Login implements Serializable {

    private String conta;
    private String senha;

    public boolean validacao() {
        return (conta != null) && (senha != null) && (!conta.isBlank()) && (!senha.isBlank());
    }
}
