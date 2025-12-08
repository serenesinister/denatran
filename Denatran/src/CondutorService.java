import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.nio.file.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CondutorService {

    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILENAME = "condutores.txt";

    private static Map<String, String> condutores = new HashMap<>();
    private static Map<String, Integer> pontos = new HashMap<>();

    private static Path logFile;

    static {
        try {
            Path logDir = Paths.get(System.getProperty("user.dir")).resolve(LOG_DIR_NAME);
            logFile = logDir.resolve(LOG_FILENAME);

            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            carregarArquivo();

        } catch (Exception e) {
            System.out.println("Erro inicializando arquivo de log: " + e.getMessage());
        }
    }

    //serviços

    public static void cadastrar(String cpf, String nome) {
        condutores.put(cpf, nome);
        pontos.put(cpf, 0);
        salvarLinha("CADASTRAR", "cpf=" + cpf + ";nome=" + nome);
        System.out.println("Condutor cadastrado com sucesso!");

        String payload = "{\"cpf\":\"" + cpf + "\",\"nome\":\"" + nome + "\"}";
        MqttUtil.publicar("detran/condutor", payload);
    }

    public static void adicionarPontos(String cpf, int p) {
        if (!pontos.containsKey(cpf)) {
            System.out.println("Condutor nao encontrado!");
            return;
        }
        pontos.put(cpf, pontos.get(cpf) + p);
        salvarLinha("PONTOS", "cpf=" + cpf + ";pontos=" + pontos.get(cpf));
        System.out.println("Pontos adicionados!");

        String payload = "{\"cpf\":\"" + cpf + "\",\"pontos\":" + pontos.get(cpf) + "}";
        MqttUtil.publicar("detran/condutor", payload);
    }

    public static void listar() {
        if (condutores.isEmpty()) {
            System.out.println("Nenhum condutor cadastrado.");
            return;
        }
        System.out.println("\n--- Condutores Cadastrados ---");
        condutores.forEach((cpf, nome) ->
            System.out.println(cpf + " - " + nome + " (" + pontos.get(cpf) + " pontos)")
        );
    }

    public static void mostrarRanking() {
        if (pontos.isEmpty()) {
            System.out.println("Nenhum condutor cadastrado.");
            return;
        }

        System.out.println("\n--- TOP 5 Maiores Pontuadores ---");

        condutores.entrySet().stream()
            .sorted((a, b) -> Integer.compare(
                    pontos.get(b.getKey()),
                    pontos.get(a.getKey())
            ))
            .limit(5)
            .forEach(e -> System.out.println(
                e.getKey() + " - " + e.getValue() + " (" + pontos.get(e.getKey()) + " pontos)"
            ));

        StringBuilder rankingJson = new StringBuilder();
        rankingJson.append("{\"ranking\":[");
        condutores.entrySet().stream()
            .sorted((a, b) -> Integer.compare(pontos.get(b.getKey()), pontos.get(a.getKey())))
            .limit(5)
            .forEach(e -> rankingJson.append("{\"cpf\":\"").append(e.getKey())
                                     .append("\",\"nome\":\"").append(e.getValue())
                                     .append("\",\"pontos\":").append(pontos.get(e.getKey()))
                                     .append("},"));
        if (rankingJson.charAt(rankingJson.length()-1) == ',') {
            rankingJson.deleteCharAt(rankingJson.length()-1);
        }
        rankingJson.append("]}");

        MqttUtil.publicar("detran/condutor", rankingJson.toString());
    }

    //subscrição MQTT

    public static void iniciarSubscricao() {
        MqttUtil.assinar("detran/condutor", (topic, msg) -> {
            System.out.println("Mensagem recebida em CondutorService: " + new String(msg.getPayload()));
        });
    }

    //persistência

    private static void salvarLinha(String acao, String dados) {
        String linha = LocalDate.now() + ";" + acao + ";" + dados;
        try (BufferedWriter bw = Files.newBufferedWriter(
                logFile,
                java.nio.charset.StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            bw.write(linha);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private static void carregarArquivo() {
        try (BufferedReader br = Files.newBufferedReader(
                logFile,
                java.nio.charset.StandardCharsets.UTF_8)) {

            String l;
            while ((l = br.readLine()) != null) {
                String[] parts = l.split(";");
                if (parts.length >= 3 && parts[1].equals("CADASTRAR")) {
                    String[] kv1 = parts[2].split("=");
                    String cpf = kv1[1];
                    String nome = parts[3].split("=")[1];
                    condutores.put(cpf, nome);
                    pontos.put(cpf, 0);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar arquivo: " + e.getMessage());
        }
    }
}