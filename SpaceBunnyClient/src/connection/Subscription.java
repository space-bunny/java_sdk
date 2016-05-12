package connection;

/**
 * A module that exports a subscription
 * @module Subscription
 */

import com.rabbitmq.client.Channel;

public class Subscription {
    private Channel channel = null;
    private String consumerTag = "";

    /**
     *
     * @constructor
     * @param channel to subscribe
     * @param consumerTag that identify channel conncetion
     */

    public Subscription(Channel channel, String consumerTag) {
        this.channel = channel;
        this.consumerTag = consumerTag;
    }

    /**
     *
     * @return open channel
     */

    public Channel getChannel() {
        return channel;
    }

    /**
     *
     * @return consumerTag that identify the open connection
     */
    public String getConsumerTag() {
        return consumerTag;
    }
}
