package io.spacebunny;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class RabbitConnection {
    private Connection conn = null;
    private SBProtocol protocol = null;
    private boolean ssl = true;
    private Map<String, SBSubscription> channelSubscribes = new HashMap<String, SBSubscription>();

    public RabbitConnection(SBProtocol protocol, boolean ssl)
    {
        this.protocol = protocol;
        this.ssl = ssl;
    }

    public boolean connect(SBDevice device) throws KeyManagementException, NoSuchAlgorithmException, IOException {
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

    }

    public boolean isConnected() {
        return ((conn != null) && (conn.isOpen()));
    }

    public void close() throws IOException {
        conn.close(0, "Close Connection");
    }

    public void publish(String device_id, SBChannel channel, String msg) throws IOException {
        Channel rabbitChannel = conn.createChannel();

        //String queueName = device_id + ".inbox";
        String exchangeName = device_id;
        String routingKey = exchangeName + "." + channel.getName();

        rabbitChannel.basicPublish(exchangeName, routingKey, null, msg.getBytes());

        rabbitChannel.close(0, "Close Channel");

    }

    public String receive(String device_id) throws IOException {
        String message = "";
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
        return message;
    }

    public void subscribe(String device_id, final OnSubscriptionMessageReceivedListener onSubscriptionMessageReceivedListener) throws IOException {
        final Channel channel = conn.createChannel();

        final String random_consumer_tag = new BigInteger(130, new SecureRandom()).toString(32);
        String queueName = device_id + ".inbox";

        channelSubscribes.put(device_id, new SBSubscription(channel, random_consumer_tag));

        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, random_consumer_tag,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException
                    {
                        String routingKey = envelope.getRoutingKey();
                        String contentType = properties.getContentType();
                        long deliveryTag = envelope.getDeliveryTag();

                        onSubscriptionMessageReceivedListener.onReceived(new String(body), envelope);

                        channel.basicAck(deliveryTag, false);
                    }
                });

    }

    public void unsubscribe(String device_id) throws IOException {
        SBSubscription subscription = channelSubscribes.get(device_id);
        subscription.getChannel().basicCancel(subscription.getConsumerTag());
        subscription.getChannel().close(0, "Close Channel");
        channelSubscribes.remove(device_id);
    }

    public interface OnSubscriptionMessageReceivedListener
    {
        void onReceived(String message, Envelope envelope);
    }

}
