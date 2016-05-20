package io.spacebunny.device;

/**
 * A module that exports a subscription
 * @module SBSubscription
 */

import com.rabbitmq.client.Channel;

public class SBSubscription {
    private Channel channel = null;
    private String consumerTag = "";

    /**
     *
     * @param channel to subscribe
     * @param consumerTag that identify channel conncetion
     */

    public SBSubscription(Channel channel, String consumerTag) {
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
