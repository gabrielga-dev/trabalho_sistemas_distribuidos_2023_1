package main.v2;

import main.v2.camadas.Persistencia;
import main.v2.camadas.Visao;

public class MainVisao {

    public static void main(String[] args) throws Exception {
        var camada = new Visao();
        camada.inicia();
        var camada2 = new Persistencia();
        camada2.inicia();
    }
}
