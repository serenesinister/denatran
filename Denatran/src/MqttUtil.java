import org.eclipse.paho.client.mqttv3.*;
import java.util.UUID;

public class MqttUtil {

    private static MqttClient client;

    static {
        try {
            String broker = "tcp://broker.hivemq.com:1883";
            String clientId = "DetranSD-" + UUID.randomUUID();
            client = new MqttClient(broker, clientId);

            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(true);

            client.connect(opts);
            System.out.println("Conectado ao broker MQTT: " + broker);
        } catch (MqttException e) {
            System.out.println("Erro ao conectar MQTT: " + e.getMessage());
        }
    }

    /**
     * Publica uma mensagem JSON no tópico MQTT.
     * @param topico Tópico (ex: detran/condutor)
     * @param payload JSON como string
     */

    public static void publicar(String topico, String payload) {
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(1);          // QoS 1 para entrega garantida
            msg.setRetained(false);  // Não reter mensagem
            client.publish(topico, msg);
            //System.out.println("Publicado em " + topico + ": " + payload);
        } catch (MqttException e) {
            System.out.println("Erro ao publicar MQTT: " + e.getMessage());
        }
    }

    /**
     * Assina um tópico MQTT.
     * @param topico Tópico (ex: detran/condutor)
     * @param listener Callback para receber mensagens
     */

    public static void assinar(String topico, IMqttMessageListener listener) {
        try {
            client.subscribe(topico, 1, listener);
        } catch (MqttException e) {
            System.out.println("Erro ao subscrever MQTT: " + e.getMessage());
        }
    }

    //desconecta do broker MQTT

    public static void desconectar() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Desconectado do broker MQTT.");
            }
        } catch (MqttException e) {
            System.out.println("Erro ao desconectar MQTT: " + e.getMessage());
        }
    }
}
