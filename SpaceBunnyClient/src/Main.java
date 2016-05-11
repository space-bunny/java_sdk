import device.Channel;
import device.Device;
import exception.SpaceBunnyConfigurationException;
import exception.SpaceBunnyConnectionException;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        String device_key = "2440a806-f9c1-4b0a-a711-20fbdefadd3e:GDZGztyXcCtnztK85yC_hA";

        try {
            final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);
            spaceBunny.setOnFinishConfigiurationListener(new SpaceBunnyClient.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(Device device) throws SpaceBunnyConnectionException {
                    //System.out.println(device.toString());
                }
            });

            spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunnyConnectionException {
                    ArrayList<Channel> channels = spaceBunny.getChannels();
                    System.out.println("5");
                    spaceBunny.publish(channels.get(0), "Ciao");
                    /*spaceBunny.subscribe(new SpaceBunnyClient.OnMessageReceivedListener() {
                        @Override
                        public void onReceived(String message) throws SpaceBunnyConnectionException {

                            System.out.println(message);

                            if (message.equals("-1")) {
                                spaceBunny.close();
                            }
                        }
                    });*/
                }
            });

        } catch (SpaceBunnyConnectionException ex) {
            ex.printStackTrace();
        }

    }
}
