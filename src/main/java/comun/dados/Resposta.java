package comun.dados;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Resposta {

    private boolean sucesso;
    private String mensagem;

    public Resposta(boolean sucesso){
        this.sucesso = sucesso;
    }
}
