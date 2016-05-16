import com.rabbitmq.client.Envelope;
import connection.RabbitConnection;
import device.Channel;
import device.Device;
import device.Protocol;
import exception.SpaceBunnyConnectionException;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        String device_key = "2440a806-f9c1-4b0a-a711-20fbdefadd3e:GDZGztyXcCtnztK85yC_hA";

        try {
            final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);
            spaceBunny.setPathCustomCA("C:\\Users\\Tommaso\\Desktop\\Fancy Pixel\\api.demo.spacebunny.io\\api.demo.spacebunny.io\\cert1.pem");
            spaceBunny.setOnFinishConfigiurationListener(new SpaceBunnyClient.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(Device device) throws SpaceBunnyConnectionException {
                    //System.out.println(device.toString());
                }
            });

            spaceBunny.setVerifyCA(false);

            spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunnyConnectionException {
                    ArrayList<Channel> channels = spaceBunny.getChannels();

                    spaceBunny.publish(channels.get(0), "Ciao");
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

            Device device = new Device.Builder()
                    .setDeviceId()
                    .setDeviceName()
                    .setHost()
                    .setSecret()
                    .setVHost()
                    .setChannels()
                    .setProtocols()
                    .getDevice();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
