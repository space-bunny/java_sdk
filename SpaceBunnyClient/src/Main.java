public class Main {
    public static void main(String[] args) {
        String device_key = "abff5045-7f56-4020-a8b8-e2a0cacae355:1inu_KfzxU9qNxW9r3uBMw";
        String protocol = "amqp";
        String channel = "data";

        SpaceBunnyClient cs = new SpaceBunnyClient(device_key);
        cs.connect(protocol);
        cs.publish(channel, "Ciao");
        cs.close();

    }
}
