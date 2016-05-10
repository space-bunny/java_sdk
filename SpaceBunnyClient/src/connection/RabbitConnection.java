package connection;

import com.rabbitmq.client.*;
import device.Device;
import device.Protocol;

public class RabbitConnection {
    private Connection conn = null;
    private Protocol protocol = null;
    private boolean ssl = true;

    public RabbitConnection(Protocol protocol, boolean ssl)
    {
        this.protocol = protocol;
        this.ssl = ssl;
    }

    public boolean connect(Device device) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(device.getHost());
            factory.setPort(ssl ? protocol.getSsl_port() : protocol.getPort());
            factory.setVirtualHost(device.getVhost());
            factory.setUsername(device.getDevice_id());
            factory.setPassword(device.getSecret());
            if (ssl)
                factory.useSslProtocol();
            conn = factory.newConnection();

            return true;

        } catch(Exception ex) {
            System.out.println("ERRORE: " + ex.toString());
            return false;
        }
    }

    public boolean isConnected() {
        return ((conn != null) && (conn.isOpen()));
    }

    public boolean close() {
        if (conn != null) {
            try {
                conn.close(0, "Close Connection");
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean publish(String device_id, device.Channel channel, String msg) {
        try {
            Channel rabbitChannel = conn.createChannel();

            //String queueName = device_id + ".inbox";
            String exchangeName = device_id;
            String routingKey = exchangeName + "." + channel.getName();

            rabbitChannel.basicPublish(exchangeName, routingKey, null, msg.getBytes());

            rabbitChannel.close(0, "Close Channel");

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String receive(String device_id) {
        String message = "";
        try {
            Channel channel = conn.createChannel();

            String queueName = device_id + ".inbox";

            GetResponse response = channel.basicGet(queueName, false);
            if (response == null) {
                message = "-1";
            } else {
                byte[] body = response.getBody();
                long deliveryTag = response.getEnvelope().getDeliveryTag();

                message = new String(body);

                channel.basicAck(deliveryTag, false);
            }

            channel.close(0, "Close Channel");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return message;
        }
    }
}
