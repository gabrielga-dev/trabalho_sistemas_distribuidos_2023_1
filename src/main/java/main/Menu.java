package main;

import comun.dados.Conta;
import comun.dados.Login;
import comun.dados.Transacao;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Scanner;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Menu {

    private static final Scanner scan = new Scanner(System.in);

    public static Integer menuLogin(){
        System.out.println("Menu login:");
        System.out.println("1 - Criar conta;");
        System.out.println("2 - Login;");
        System.out.print("Sua escolha: ");
        return Integer.parseInt(scan.nextLine());
    }

    public static Integer menuPrincipal() {
        System.out.println("Menu principal:");
        System.out.println("1 - Transferir dinheiro;");
        System.out.println("2 - Conferir saldo;");
        System.out.println("3 - Sair;");
        System.out.print("Sua escolha: ");
        return Integer.parseInt(scan.nextLine());
    }

    public static Conta criaConta() {
        var conta = new Conta();

        conta.setIdentificador(UUID.randomUUID().toString());
        conta.getTransacoes().get(0).setIdentificadorRecebedor(conta.getIdentificador());

        System.out.print("Digite o seu nome: ");
        conta.setNomeCliente(scan.nextLine());

        System.out.print("Digite a sua senha: ");
        conta.setSenha(scan.nextLine());

        return conta;
    }

    public static Login criaLogin() {
        var login = new Login();

        System.out.print("Digite a conta: ");
        login.setConta(scan.nextLine());

        System.out.print("Digite a sua senha: ");
        login.setSenha(scan.nextLine());

        return login;
    }

    public static Transacao criaTransferencia(String contaPagadora) {
        var transacao = new Transacao();

        transacao.setIdentificadorPagador(contaPagadora);

        System.out.print("Digite a conta para a qual estará enciando o dinheiro: ");
        transacao.setIdentificadorRecebedor(scan.nextLine());

        System.out.print("Digite o valor a ser transferido: ");
        transacao.setValor(Long.valueOf(scan.nextLine()));

        return transacao;
    }
}
