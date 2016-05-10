import device.Channel;
import device.Device;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        String device_key = "2440a806-f9c1-4b0a-a711-20fbdefadd3e:GDZGztyXcCtnztK85yC_hA";

        SpaceBunnyClient cs = new SpaceBunnyClient(device_key);
        cs.setOnFinishConfigiuration(new SpaceBunnyClient.OnFinishConfigiurationCallBack() {
            @Override
            public void onConfigured(Device device, boolean ssl) {
                System.out.println(device.toString());
            }
        });

        cs.connect();
        ArrayList<Channel> channels = cs.getChannels();
        if (cs.isConnected()) {

            cs.publish(channels.get(0), "Ciao");

            cs.receive(new SpaceBunnyClient.OnMessageReceived() {
                @Override
                public void onReceived(String message) {

                }
            });
        }

        cs.close();

    }
}
