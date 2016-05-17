package io.spacebunny;

import com.rabbitmq.client.Envelope;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        String device_key = "2440a806-f9c1-4b0a-a711-20fbdefadd3e:GDZGztyXcCtnztK85yC_hA";

        try {
            final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);
            //spaceBunny.setPathCustomCA("C:\\Users\\Tommaso\\Desktop\\Fancy Pixel\\api.demo.spacebunny.io\\api.demo.spacebunny.io\\cert1.pem");
            spaceBunny.setOnFinishConfigiurationListener(new SpaceBunnyClient.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(SBDevice device) throws SpaceBunnyConnectionException {
                    System.out.println(device.toString());
                }
            });

            spaceBunny.setVerifyCA(false);

            spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunnyConnectionException {
                    ArrayList<SBChannel> channels = spaceBunny.getChannels();

                    spaceBunny.publish(channels.get(0), "{temp: 1}");
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
