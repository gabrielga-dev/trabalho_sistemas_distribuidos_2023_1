package comun.dados;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Estado implements Serializable {

    private List<Conta> contas;

    public Estado(){
        this.contas = new ArrayList<>();
    }
}
