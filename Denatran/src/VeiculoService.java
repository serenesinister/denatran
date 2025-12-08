import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class VeiculoService {

    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILENAME = "veiculos.txt";

    private static Path logFile;

    private static class Veiculo {
        String placa;
        String modelo;
        double valor;
        String cpfProprietario;
        int ano;

        Veiculo(String placa, String modelo, double valor, String cpf, int ano) {
            this.placa = placa;
            this.modelo = modelo;
            this.valor = valor;
            this.cpfProprietario = cpf;
            this.ano = ano;
        }
    }

    private static Map<String, Veiculo> veiculos = new HashMap<>();

    //inicialização (cria arquivo e carrega)
    
    static {
        try {
            Path logDir = Paths.get(System.getProperty("user.dir")).resolve(LOG_DIR_NAME);
            if (!Files.exists(logDir)) Files.createDirectories(logDir);

            logFile = logDir.resolve(LOG_FILENAME);
            if (!Files.exists(logFile)) Files.createFile(logFile);

            carregarArquivo();

        } catch (Exception e) {
            System.out.println("Erro ao iniciar VeiculoService: " + e.getMessage());
        }
    }

    //operações

    public static void emplacarVeiculo(String placa, String modelo, double valor, String cpf, int ano) {
        if (veiculos.containsKey(placa)) {
            System.out.println("Placa já cadastrada!");
            return;
        }

        Veiculo v = new Veiculo(placa, modelo, valor, cpf, ano);
        veiculos.put(placa, v);

        salvarLinha("EMPLACAR",
                "placa=" + placa +
                ";modelo=" + modelo +
                ";valor=" + valor +
                ";cpf=" + cpf +
                ";ano=" + ano);

        IPVAService.registrarVeiculo(placa, modelo, valor, cpf, ano);

        System.out.println("Veículo emplacado com sucesso!");

        String payload = "{\"acao\":\"emplacar\",\"placa\":\"" + placa +
                         "\",\"modelo\":\"" + modelo +
                         "\",\"valor\":" + valor +
                         ",\"cpf\":\"" + cpf +
                         "\",\"ano\":" + ano + "}";
        MqttUtil.publicar("detran/veiculo", payload);
    }

    public static void transferirProprietario(String placa, String novoCpf) {
        if (!veiculos.containsKey(placa)) {
            System.out.println("Veículo não encontrado!");
            return;
        }

        Veiculo v = veiculos.get(placa);
        v.cpfProprietario = novoCpf;

        salvarLinha("TRANSFERENCIA",
                "placa=" + placa + ";novo_cpf=" + novoCpf);

        IPVAService.registrarVeiculo(placa, v.modelo, v.valor, novoCpf, v.ano);

        System.out.println("Transferência realizada!");

        String payload = "{\"acao\":\"transferencia\",\"placa\":\"" + placa +
                         "\",\"novoCpf\":\"" + novoCpf + "\"}";
        MqttUtil.publicar("detran/veiculo", payload);
    }

    public static void consultarPorAno(int ano) {
        System.out.println("\n--- Veículos emplacados em " + ano + " ---");
        boolean achou = false;

        for (Veiculo v : veiculos.values()) {
            if (v.ano == ano) {
                achou = true;
                System.out.println(v.placa + " - " + v.modelo +
                        " | Valor: R$ " + v.valor +
                        " | Dono: " + v.cpfProprietario);
            }
        }
        if (!achou) System.out.println("Nenhum veículo encontrado!");
    }

    public static void consultarPorCondutor(String cpf) {
        System.out.println("\n--- Veículos do condutor: " + cpf + " ---");
        boolean achou = false;

        for (Veiculo v : veiculos.values()) {
            if (v.cpfProprietario.equals(cpf)) {
                achou = true;
                System.out.println(v.placa + " - " + v.modelo + " | Ano: " + v.ano);
            }
        }
        if (!achou) System.out.println("Nenhum veículo localizado!");
    }

    public static void listarTodos() {
        System.out.println("\n--- Todos os veículos emplacados ---");
        if (veiculos.isEmpty()) {
            System.out.println("Nenhum veículo encontrado.");
            return;
        }
        for (Veiculo v : veiculos.values()) {
            System.out.println(
                    v.placa + " - " + v.modelo +
                    " | Ano: " + v.ano +
                    " | Valor: R$" + v.valor +
                    " | Proprietário: " + v.cpfProprietario
            );
        }
    }

    //métodos auxiliares

    public static boolean existeVeiculo(String placa) {
        return veiculos.containsKey(placa);
    }

    public static String getProprietario(String placa) {
        Veiculo v = veiculos.get(placa);
        return (v != null ? v.cpfProprietario : null);
    }

    public static String getPlacaPorCondutor(String cpf) {
        for (Veiculo v : veiculos.values()) {
            if (v.cpfProprietario.equals(cpf)) return v.placa;
        }
        return null;
    }

    //subscriçãoMQTT

    public static void iniciarSubscricao() {
        MqttUtil.assinar("detran/veiculo", (topic, msg) -> {
            System.out.println("Mensagem recebida em VeiculoService: " + new String(msg.getPayload()));
        });
    }

    //persistência

    private static void salvarLinha(String acao, String dados) {
        try (BufferedWriter bw = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            bw.write(System.currentTimeMillis() + ";" + acao + ";" + dados);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Erro gravando arquivo: " + e.getMessage());
        }
    }

    private static void carregarArquivo() {
        try (BufferedReader br = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(";");
                if (p.length < 3) continue;

                String acao = p[1];
                Map<String, String> map = parse(p[2]);

                if (acao.equals("EMPLACAR")) {
                    Veiculo v = new Veiculo(
                            map.get("placa"),
                            map.get("modelo"),
                            Double.parseDouble(map.get("valor")),
                            map.get("cpf"),
                            Integer.parseInt(map.get("ano"))
                    );
                    veiculos.put(v.placa, v);
                }

                if (acao.equals("TRANSFERENCIA")) {
                    String placa = map.get("placa");
                    String novoCpf = map.get("novo_cpf");
                    if (veiculos.containsKey(placa)) {
                        veiculos.get(placa).cpfProprietario = novoCpf;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar arquivo de veículos: " + e.getMessage());
        }
    }

    private static Map<String, String> parse(String s) {
        Map<String, String> map = new HashMap<>();
        for (String par : s.split(";")) {
            String[] kv = par.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}