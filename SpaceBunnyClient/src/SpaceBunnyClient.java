import com.rabbitmq.client.*;
import com.sun.istack.internal.Nullable;
import config.Costants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import connection.RabbitConnection;
import device.*;
import org.json.JSONObject;

import javax.net.ssl.*;

public class SpaceBunnyClient {

    private Device device = null;
    private RabbitConnection rabbitConnection = null;

    private OnFinishConfigiurationCallBack configCallBack = null;

    private String device_key;
    private boolean ssl = true;

    public SpaceBunnyClient(String device_key) {
        this.device_key = device_key;
        configure();
    }

    public SpaceBunnyClient(String device_key, boolean ssl) {
        this.device_key = device_key;
        this.ssl = ssl;
        configure();
    }

    private boolean configure() {

        URLConnection uc;
        BufferedReader reader;

        // TODO Certificato custom

        try {

            URL url = new URL(Costants.generateHostname(ssl));
            uc = url.openConnection();
            uc.setRequestProperty("Device-Key", device_key);
            reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));

        } catch (Exception ex) {

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                return false;
            }

            // Now you can access an https URL without having the certificate in the truststore
            try {
                URL url = new URL(Costants.generateHostname(ssl));
                uc = url.openConnection();
                uc.setRequestProperty("Device-Key", device_key);
                reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
            } catch (Exception e) {
                return false;
            }

        }

        try {

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = reader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            device = new Device(new JSONObject(responseStrBuilder.toString()));

            if (configCallBack != null)
                configCallBack.onConfigured(this.device, this.ssl);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public void setOnFinishConfigiuration(OnFinishConfigiurationCallBack callBack) {
        configCallBack = callBack;
    }

    public boolean connect() {
        Protocol protocol = findProtocol(Costants.DEFAULT_PROTOCOL);
        if (protocol == null)
            return false;
        rabbitConnection = new RabbitConnection(protocol, ssl);
        return rabbitConnection.connect(device);
    }

    public boolean connect(Protocol protocol) {
        rabbitConnection = new RabbitConnection(protocol, ssl);
        return rabbitConnection.connect(device);
    }

    public boolean isConnected() {
        return rabbitConnection.isConnected();
    }

    public boolean publish(device.Channel channel, String msg) {
        if (rabbitConnection.isConnected()) {
            System.out.print("\nTry publish \"" + msg + "\" to " + channel.getName() + " channel...\n");
            return rabbitConnection.publish(device.getDevice_id(), channel, msg);
        } return false;
    }

    public void receive(OnMessageReceived onMessageReceived) {
        if (rabbitConnection.isConnected()) {
            onMessageReceived.onReceived(rabbitConnection.receive(device.getDevice_id()));
        }
    }

    private Protocol findProtocol(String p) {
        for (Protocol protocol : device.getProtocols())
            if (protocol.getName().equals(p))
                return protocol;
        return null;
    }

    public ArrayList<Protocol> getProtocols() {
        return device.getProtocols();
    }

    public ArrayList<device.Channel> getChannels() {
        return device.getChannels();
    }

    public boolean close() {
        return rabbitConnection.close();
    }

    public String toString() {
        return device.toString(); // TODO add connection status
    }

    public interface OnFinishConfigiurationCallBack
    {
        void onConfigured(Device device, boolean ssl);
    }

    public interface OnMessageReceived
    {
        void onReceived(String message);
    }

}
