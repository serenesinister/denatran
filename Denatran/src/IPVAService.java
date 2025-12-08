import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class IPVAService {

    private static final String LOG_DIR_NAME = "logs";
    private static final String VEICULO_FILE = "veiculos.txt";
    private static Path veiculoFile;

    private static class Veiculo {
        String placa;
        String modelo;
        double valor;
        String cpfProprietario;
        int ano;
    }

    private static Map<String, Veiculo> mapaVeiculos = new HashMap<>();

    static {
        try {
            Path logDir = Paths.get(System.getProperty("user.dir")).resolve(LOG_DIR_NAME);
            if (!Files.exists(logDir)) Files.createDirectories(logDir);

            veiculoFile = logDir.resolve(VEICULO_FILE);
            if (!Files.exists(veiculoFile)) Files.createFile(veiculoFile);

            carregarArquivoVeiculos();

        } catch (Exception e) {
            System.out.println("Erro ao iniciar IPVAService: " + e.getMessage());
        }
    }

    //registrar ou atualizar

    public static void registrarVeiculo(String placa, String modelo, double valor, String cpf, int ano) {
        Veiculo v = new Veiculo();
        v.placa = placa;
        v.modelo = modelo;
        v.valor = valor;
        v.cpfProprietario = cpf;
        v.ano = ano;

        mapaVeiculos.put(placa, v);
        salvarArquivoCompleto();
    }

    //consultas

    public static void calcularIPVA(String placa) {
        Veiculo v = mapaVeiculos.get(placa);

        if (v == null) {
            System.out.println("Veículo não encontrado!");
            return;
        }

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);

        if (anoAtual - v.ano > 20) {
            System.out.printf("\nVeículo %s (%s) isento de IPVA!\n\n", v.placa, v.modelo);
            return;
        }

        double ipva = v.valor * 0.02;
        System.out.printf("\nIPVA do veículo %s (%s): R$ %.2f\n\n", v.placa, v.modelo, ipva);
    }

    public static void listarTodos() {
        if (mapaVeiculos.isEmpty()) {
            System.out.println("Nenhum veículo registrado!");
            return;
        }

        System.out.println("\n===== TODOS VEÍCULOS / IPVA =====");
        for (Veiculo v : mapaVeiculos.values()) {
            System.out.printf("Placa: %s | Modelo: %s | Ano: %d | Valor: R$ %.2f\n",
                    v.placa, v.modelo, v.ano, v.valor);
        }
        System.out.println("=================================\n");
    }

    //persistência

    private static void salvarArquivoCompleto() {
        try {
            List<String> linhas = new ArrayList<>();

            for (Veiculo v : mapaVeiculos.values()) {
                linhas.add(
                        "VEICULO;"
                                + "placa=" + v.placa + ";"
                                + "modelo=" + v.modelo + ";"
                                + "valor=" + v.valor + ";"
                                + "cpf=" + v.cpfProprietario + ";"
                                + "ano=" + v.ano
                );
            }

            Files.write(veiculoFile, linhas, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            System.out.println("Erro ao salvar veículos: " + e.getMessage());
        }
    }

    private static void carregarArquivoVeiculos() {
        try (BufferedReader br = Files.newBufferedReader(veiculoFile, StandardCharsets.UTF_8)) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(";");
                if (p.length < 6 || !p[0].equals("VEICULO")) continue;

                Veiculo v = new Veiculo();
                v.placa = p[1].split("=")[1];
                v.modelo = p[2].split("=")[1];
                v.valor = Double.parseDouble(p[3].split("=")[1]);
                v.cpfProprietario = p[4].split("=")[1];
                v.ano = Integer.parseInt(p[5].split("=")[1]);

                mapaVeiculos.put(v.placa, v);
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar veículos: " + e.getMessage());
        }
    }

    //subscriçãoMQTT

    public static void iniciarSubscricao() {
        MqttUtil.assinar("detran/ipva", (topic, msg) -> {
            System.out.println("Mensagem recebida em IPVAService: " + new String(msg.getPayload()));
        });
    }

}