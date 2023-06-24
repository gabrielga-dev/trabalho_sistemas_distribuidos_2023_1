package comun.protocolo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Protocolo implements Serializable {

    private boolean resposta;
    private String conteudo;
    private TipoProtocolo tipo;
}
