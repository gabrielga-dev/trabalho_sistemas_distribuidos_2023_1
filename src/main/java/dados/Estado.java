package dados;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Estado implements Serializable {

    private List<Conta> contas;
}
