import com.rabbitmq.client.ConnectionFactory;
import config.Costants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import device.Device;
import device.Protocol;
import org.json.JSONObject;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class SpaceBunnyClient {

    private Device device = null;
    private String device_key;
    private Connection conn;

    public SpaceBunnyClient(String device_key) {
        this.device_key = device_key;
    }

    public void connect(String protocol) {
        try {

            URL url = new URL(Costants.generateHostname());
            URLConnection uc = url.openConnection();

            uc.setRequestProperty("Device-Key", device_key);

            BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = reader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            device = new Device(new JSONObject(responseStrBuilder.toString()));
            //System.out.println("DEVICE CONFIGURATO.\n" + device);

            try {
                System.out.print("CONNECTION...");
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(device.host);
                factory.setPort(findProtocol(protocol).getPort());
                factory.setVirtualHost(device.vhost);
                factory.setUsername(device.device_id);
                factory.setPassword(device.secret);
                conn = factory.newConnection();
                System.out.println("" + conn.getHost());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch(Exception ex) {
            System.out.println("ERRORE: " + ex.toString());
        }


    }

    public void publish(String channel_name, String msg) {
        System.out.print("PUBLISH");
        try {
            Channel channel = conn.createChannel();

            String queueName = device.device_id + ".inbox";
            String exchangeName = device.device_id;
            String routingKey = exchangeName + "." + findChannelID(channel_name).getName();

            channel.exchangeDeclare(exchangeName, "direct", true);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, routingKey);

            int i = 0;
            while (i < 20) {
                channel.basicPublish("", findChannelID(channel_name).getName(), null, msg.getBytes());
                System.out.print("|");
                i++;
                Thread.sleep(1000);
            }

            channel.close(0, "Close Channel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Protocol findProtocol(String p) {
        for (Protocol protocol : device.protocols)
            if (protocol.getName().equals(p))
                return protocol;
        return null;
    }

    public device.Channel findChannelID(String c) {
        for (device.Channel ch : device.channels)
            if (ch.getName().equals(c))
                return ch;
        return null;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close(0, "Close Connection");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String toString() {
        return device.toString(); // TODO add connection status
    }

}
