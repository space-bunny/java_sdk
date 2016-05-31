import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Envelope;
import io.spacebunny.SpaceBunny;import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBDevice;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JavaSample {

    public static void main(String[] args) {

        String liveStream_key_client = "ynPDRX2szb1Sj3sNvk23VnCmdRCKZyZmPJBmceaqzb4";
        String liveStream_key_secret = "hLE2z3WgRmkzsA3SKSm5tZED6usbWiTmiwH7xugZA7E";

        try {
            final SpaceBunny.LiveStream spaceBunnyLS = new SpaceBunny.LiveStream(liveStream_key_client, liveStream_key_secret);

            spaceBunnyLS.connect(new SpaceBunny.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunny.ConnectionException {
                    spaceBunnyLS.subscribe("JavaStream", false, new RabbitConnection.OnSubscriptionMessageReceivedListener() {
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
                        TimeUnit.SECONDS.sleep(20);
                        spaceBunnyLS.unsubscribe("JavaStream", false);
                        spaceBunnyLS.close();
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
