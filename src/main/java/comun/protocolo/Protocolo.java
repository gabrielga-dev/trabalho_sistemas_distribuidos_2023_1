package comun.protocolo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Protocolo implements Serializable {

    private boolean resposta = false;
    private String conteudo;
    private TipoProtocolo tipo;

    public Protocolo(String conteudo, TipoProtocolo tipo){
        this.conteudo = conteudo;
        this.tipo = tipo;
    }
}
