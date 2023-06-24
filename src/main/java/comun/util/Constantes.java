package comun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constantes {

    public static String PASTA_ARQUIVOS = "/Users/gabriel/Documents/IFMG/p9/sistemas_distribuidos/trabalho_pratico/arquivos/";
    public static String ARQUIVOS_XML = "/Users/gabriel/Documents/IFMG/p9/sistemas_distribuidos/trabalho_pratico/arquivos/xml/";
    public static String XML_TESTE = "/Users/gabriel/Documents/IFMG/p9/sistemas_distribuidos/trabalho_pratico/arquivos/xml/teste.xml";
    public static String XML_CONFIG = "/Users/gabriel/Documents/IFMG/p9/sistemas_distribuidos/trabalho_pratico/arquivos/xml/config.xml";
    public static String ARQUIVO_ESTADO_PATH = "/Users/gabriel/Documents/IFMG/p9/sistemas_distribuidos/trabalho_pratico/estado/estado.json";
    public static final Long VALOR_SALDO_INICIAL = 1000L;

    public static final String CANAL_VISAO = "TRABALHO_SD_CANAL_VISAO";
    public static final String CANAL_CONTROLE = "TRABALHO_SD_CANAL_CONTROLER";
    public static final String CANAL_PERSISTENCIA = "TRABALHO_SD_CANAL_PERSISTENCIA";
}
