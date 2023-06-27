package main.v2;

import main.v2.camadas.Persistencia;

public class MainPersistencia {

    public static void main(String[] args) throws Exception {
        var camada = new Persistencia();
        camada.inicia();
    }
}
