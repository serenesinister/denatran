import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        CondutorService.iniciarSubscricao();
        VeiculoService.iniciarSubscricao();
        MultaService.iniciarSubscricao();
        IPVAService.iniciarSubscricao();

        System.out.println("===== SISTEMA DENATRAN =====");
        System.out.println("Microsserviços conectados ao MQTT Broker...");

        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1 - Cadastrar Condutor");
            System.out.println("2 - Emplacar Veículo");
            System.out.println("3 - Transferir Proprietário");
            System.out.println("4 - Lançar Multa");
            System.out.println("5 - Consultar Veículos por Ano");
            System.out.println("6 - Consultar Multas por Condutor");
            System.out.println("7 - Consultar Multas por Ano");
            System.out.println("8 - Consultar Multas por Veículo");
            System.out.println("9 - Ranking de Pontuação");
            System.out.println("10 - Calcular IPVA por Placa");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            String op = sc.nextLine();

            switch (op) {
                case "1": cadastrarCondutor(); break;
                case "2": emplacarVeiculo(); break;
                case "3": transferirVeiculo(); break;
                case "4": lancarMulta(); break;
                case "5": consultarVeiculosAno(); break;
                case "6": consultarMultasCPF(); break;
                case "7": consultarMultasAno(); break;
                case "8": consultarMultasPlaca(); break;
                case "9": CondutorService.mostrarRanking(); break;
                case "10": consultarIPVA(); break;
                case "0":
                    System.out.println("Encerrando sistema...");
                    MqttUtil.desconectar();
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private static void cadastrarCondutor() {
        System.out.print("CPF: ");
        String cpf = sc.nextLine();
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        CondutorService.cadastrar(cpf, nome);
    }

    private static void emplacarVeiculo() {
        System.out.print("Placa: ");
        String placa = sc.nextLine();
        System.out.print("Modelo: ");
        String modelo = sc.nextLine();
        System.out.print("Valor: ");
        double valor = Double.parseDouble(sc.nextLine());
        System.out.print("CPF do proprietário: ");
        String cpf = sc.nextLine();
        System.out.print("Ano: ");
        int ano = Integer.parseInt(sc.nextLine());
        VeiculoService.emplacarVeiculo(placa, modelo, valor, cpf, ano);
    }

    private static void transferirVeiculo() {
        System.out.print("Placa: ");
        String placa = sc.nextLine();
        System.out.print("Novo Proprietário (CPF): ");
        String cpf = sc.nextLine();
        VeiculoService.transferirProprietario(placa, cpf);
    }

    private static void lancarMulta() {
        System.out.print("CPF do Condutor: ");
        String cpf = sc.nextLine();
        System.out.print("Descrição da multa: ");
        String desc = sc.nextLine();
        System.out.print("Valor da multa: ");
        double valor = Double.parseDouble(sc.nextLine());
        MultaService.lancarMulta(cpf, desc, valor);
    }

    private static void consultarVeiculosAno() {
        System.out.print("Ano dos veículos: ");
        int ano = Integer.parseInt(sc.nextLine());
        VeiculoService.consultarPorAno(ano);
    }

    private static void consultarMultasCPF() {
        System.out.print("CPF do condutor: ");
        String cpf = sc.nextLine();
        MultaService.consultarMultas(cpf);
    }

    private static void consultarMultasAno() {
        System.out.print("Ano das multas: ");
        int ano = Integer.parseInt(sc.nextLine());
        MultaService.consultarMultasPorAno(ano);
    }

    private static void consultarMultasPlaca() {
        System.out.print("Placa do veículo: ");
        String placa = sc.nextLine();
        MultaService.consultarMultasPorVeiculo(placa);
    }

    private static void consultarIPVA() {
        System.out.print("Placa: ");
        String placa = sc.nextLine();
        IPVAService.calcularIPVA(placa);
    }
}
