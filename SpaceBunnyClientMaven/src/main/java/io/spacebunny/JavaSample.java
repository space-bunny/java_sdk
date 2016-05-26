package io.spacebunny;

import com.rabbitmq.client.ConfirmListener;
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBDevice;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JavaSample {

    public static void main(String[] args) {

        String device_key = "96828474-e65e-4518-a084-0710be15495b:zv29Ten2sxZGb1ccW2yQLw";

        try {
            final SpaceBunny.Client spaceBunny = new SpaceBunny.Client(device_key);

            spaceBunny.setOnFinishConfigiurationListener(new SpaceBunny.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(SBDevice device) throws SpaceBunny.ConnectionException {
                    //System.out.println(device.toString());
                }
            });

            spaceBunny.connect(new SpaceBunny.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunny.ConnectionException {
                    spaceBunny.publish("alarms", "{temp: 2}", null, new ConfirmListener() {
                        @Override
                        public void handleAck(long l, boolean b) throws IOException {
                            System.out.println("ACK OK");
                        }

                        @Override
                        public void handleNack(long l, boolean b) throws IOException {

                        }
                    });
                    spaceBunny.subscribe(new RabbitConnection.OnSubscriptionMessageReceivedListener() {
                        @Override
                        public void onReceived(String s, com.rabbitmq.client.Envelope envelope) {
                            System.out.println(s);
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
