package io.spacebunny;

import com.rabbitmq.client.Envelope;
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBChannel;
import io.spacebunny.device.SBDevice;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        String device_key = "98ee7883-db36-4690-9fde-63d363902bb2:nthoGiwqQz9sh1tYDNz6xw";

        try {
            final SpaceBunny.Client spaceBunny = new SpaceBunny.Client(device_key);

            spaceBunny.setOnFinishConfigiurationListener(new SpaceBunny.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(SBDevice device) throws SpaceBunny.ConnectionException {
                    System.out.println(device.toString());
                }
            });

            spaceBunny.connect(new SpaceBunny.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunny.ConnectionException {
                    spaceBunny.publish("alarms", "{temp: 2}", null, null);
                    spaceBunny.subscribe(new RabbitConnection.OnSubscriptionMessageReceivedListener() {
                        @Override
                        public void onReceived(String message, Envelope envelope) {
                            System.out.println(message);
                        }
                    });
                }
            });

            new Thread() {
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                        spaceBunny.unsubscribe();
                        spaceBunny.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
