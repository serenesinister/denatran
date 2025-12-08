import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MultaService {

    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILENAME = "multas.txt";

    private static Path logFile;

    private static class Multa {
        String cpf;
        String placa;
        String descricao;
        double valor;
        int ano;

        Multa(String cpf, String placa, String descricao, double valor, int ano) {
            this.cpf = cpf;
            this.placa = placa;
            this.descricao = descricao;
            this.valor = valor;
            this.ano = ano;
        }
    }

    private static Map<String, List<Multa>> multasPorCPF = new HashMap<>();

    //inicialização

    static {
        try {
            Path logDir = Paths.get(System.getProperty("user.dir")).resolve(LOG_DIR_NAME);
            if (!Files.exists(logDir)) Files.createDirectories(logDir);

            logFile = logDir.resolve(LOG_FILENAME);
            if (!Files.exists(logFile)) Files.createFile(logFile);

            carregarArquivo();

        } catch (Exception e) {
            System.out.println("Erro ao iniciar MultaService: " + e.getMessage());
        }
    }

    //operações
    
    public static void lancarMulta(String cpf, String descricao, double valor) {
        String placa = VeiculoService.getPlacaPorCondutor(cpf);

        if (placa == null) {
            System.out.println("Condutor não possui veículo cadastrado!");
            return;
        }

        int anoAtual = java.time.Year.now().getValue();
        Multa m = new Multa(cpf, placa, descricao, valor, anoAtual);

        multasPorCPF.computeIfAbsent(cpf, k -> new ArrayList<>()).add(m);

        salvarLinha("MULTA",
                "cpf=" + cpf +
                ";placa=" + placa +
                ";descricao=" + descricao +
                ";valor=" + valor +
                ";ano=" + anoAtual);

        CondutorService.adicionarPontos(cpf, 5);

        System.out.println("Multa registrada com sucesso!");

        String payload = "{\"acao\":\"multa_lancada\",\"cpf\":\"" + cpf +
                         "\",\"placa\":\"" + placa +
                         "\",\"descricao\":\"" + descricao +
                         "\",\"valor\":" + valor +
                         ",\"ano\":" + anoAtual + "}";
        MqttUtil.publicar("detran/multa", payload);
    }

    public static void consultarMultas(String cpf) {
        System.out.println("\n--- Multas do Condutor: " + cpf + " ---");

        List<Multa> multas = multasPorCPF.get(cpf);
        if (multas == null || multas.isEmpty()) {
            System.out.println("Nenhuma multa encontrada.");
            return;
        }

        multas.forEach(m -> System.out.println(
                m.placa + " | " + m.descricao + " | Ano: " + m.ano + " | R$ " + m.valor
        ));

        StringBuilder payload = new StringBuilder("{\"acao\":\"consultar_multas\",\"cpf\":\"" + cpf + "\",\"multas\":[");
        for (Multa m : multas) {
            payload.append("{\"placa\":\"").append(m.placa)
                   .append("\",\"descricao\":\"").append(m.descricao)
                   .append("\",\"valor\":").append(m.valor)
                   .append(",\"ano\":").append(m.ano).append("},");
        }
        if (payload.charAt(payload.length()-1) == ',') payload.deleteCharAt(payload.length()-1);
        payload.append("]}");

        MqttUtil.publicar("detran/multa", payload.toString());
    }

    public static void consultarMultasPorAno(int ano) {
        System.out.println("\n--- Multas do ano " + ano + " ---");
        boolean achou = false;

        List<Multa> resultado = new ArrayList<>();
        for (List<Multa> lista : multasPorCPF.values()) {
            for (Multa m : lista) {
                if (m.ano == ano) {
                    achou = true;
                    resultado.add(m);
                    System.out.println("CPF: " + m.cpf + " | Placa: " + m.placa +
                            " | " + m.descricao + " | R$" + m.valor);
                }
            }
        }

        if (!achou) System.out.println("Nenhuma multa neste ano.");

        StringBuilder payload = new StringBuilder("{\"acao\":\"consultar_multas_ano\",\"ano\":" + ano + ",\"multas\":[");
        for (Multa m : resultado) {
            payload.append("{\"cpf\":\"").append(m.cpf)
                   .append("\",\"placa\":\"").append(m.placa)
                   .append("\",\"descricao\":\"").append(m.descricao)
                   .append("\",\"valor\":").append(m.valor)
                   .append("},");
        }
        if (payload.charAt(payload.length()-1) == ',') payload.deleteCharAt(payload.length()-1);
        payload.append("]}");

        MqttUtil.publicar("detran/multa", payload.toString());
    }

    public static void consultarMultasPorVeiculo(String placa) {
        System.out.println("\n--- Multas do veículo: " + placa + " ---");
        boolean achou = false;
        List<Multa> resultado = new ArrayList<>();

        for (List<Multa> lista : multasPorCPF.values()) {
            for (Multa m : lista) {
                if (m.placa.equals(placa)) {
                    achou = true;
                    resultado.add(m);
                    System.out.println("CPF: " + m.cpf + " | " +
                            m.descricao + " | Ano: " + m.ano + " | R$" + m.valor);
                }
            }
        }

        if (!achou) System.out.println("Nenhuma multa para essa placa!");

        StringBuilder payload = new StringBuilder("{\"acao\":\"consultar_multas_placa\",\"placa\":\"" + placa + "\",\"multas\":[");
        for (Multa m : resultado) {
            payload.append("{\"cpf\":\"").append(m.cpf)
                   .append("\",\"descricao\":\"").append(m.descricao)
                   .append("\",\"valor\":").append(m.valor)
                   .append(",\"ano\":").append(m.ano).append("},");
        }
        if (payload.charAt(payload.length()-1) == ',') payload.deleteCharAt(payload.length()-1);
        payload.append("]}");

        MqttUtil.publicar("detran/multa", payload.toString());
    }

    //subscriçãoMQTT
    
    public static void iniciarSubscricao() {
        MqttUtil.assinar("detran/multa", (topic, msg) -> {
            System.out.println("Mensagem recebida em MultaService: " + new String(msg.getPayload()));
        });
    }

    //arquivo
    
    private static void salvarLinha(String acao, String dados) {
        try (BufferedWriter bw = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            bw.write(System.currentTimeMillis() + ";" + acao + ";" + dados);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Erro gravando arquivo de multas: " + e.getMessage());
        }
    }

    private static void carregarArquivo() {
        if (!Files.exists(logFile)) return;

        try (BufferedReader br = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.trim().isEmpty()) continue;
                String[] p = l.split(";");
                if (p.length < 3) continue;

                String acao = p[1];
                Map<String, String> map = parse(p[2]);

                if (acao.equals("MULTA")) {
                    Multa m = new Multa(
                            map.get("cpf"),
                            map.get("placa"),
                            map.get("descricao"),
                            Double.parseDouble(map.get("valor")),
                            Integer.parseInt(map.get("ano"))
                    );
                    multasPorCPF.computeIfAbsent(m.cpf, k -> new ArrayList<>()).add(m);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar multas: " + e.getMessage());
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