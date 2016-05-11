import com.sun.istack.internal.Nullable;
import config.Costants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import connection.RabbitConnection;
import device.*;
import exception.SpaceBunnyConnectionException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.*;

public class SpaceBunnyClient {

    private Device device = null;
    private RabbitConnection rabbitConnection = null;

    private OnFinishConfigiurationListener configCallBack = null;

    private String device_key;
    private boolean ssl = true;

    public SpaceBunnyClient(String device_key) throws SpaceBunnyConnectionException {
        if (device_key == null || device_key.equals(""))
            throw new SpaceBunnyConnectionException("Configuration error.");
        this.device_key = device_key;
    }

    public SpaceBunnyClient(Device device) {
        this.device = device;
    }

    private void configure() throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException, SpaceBunnyConnectionException {

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

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());


            URL url = new URL(Costants.generateHostname(ssl));
            uc = url.openConnection();
            uc.setRequestProperty("Device-Key", device_key);
            reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));

        }

        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = reader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        device = new Device(new JSONObject(responseStrBuilder.toString()));

        if (configCallBack != null)
            configCallBack.onConfigured(this.device);

    }

    public void setOnFinishConfigiurationListener(@Nullable OnFinishConfigiurationListener callBack) {
        configCallBack = callBack;
    }

    public void connect() throws SpaceBunnyConnectionException {
        connect(null, null);
    }

    public void connect(OnConnectedListener onConnectedListener) throws SpaceBunnyConnectionException {
        connect(null, onConnectedListener);
    }

    public void connect(Protocol protocol, OnConnectedListener onConnectedListener) throws SpaceBunnyConnectionException {
        try {
            configure();
            if (protocol == null)
                protocol = findProtocol(Costants.DEFAULT_PROTOCOL);
            rabbitConnection = new RabbitConnection(protocol, ssl);
            if (rabbitConnection.connect(device) && onConnectedListener != null)
                onConnectedListener.onConnected();
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
    }

    public boolean isConnected() {
        return rabbitConnection.isConnected();
    }


    public void publish(device.Channel channel, String msg) throws SpaceBunnyConnectionException {
        if (rabbitConnection.isConnected()) {
            try {
                rabbitConnection.publish(device.getDevice_id(), channel, msg);
            } catch (Exception ex) {
                throw new SpaceBunnyConnectionException(ex);
            }
        }
        System.out.println("5");
        throw new SpaceBunnyConnectionException("Space Bunny is not connected. Try spaceBunny.connect().");
    }

    public void receive(OnMessageReceivedListener onMessageReceived) throws SpaceBunnyConnectionException {
        if (rabbitConnection.isConnected()) {
            try {
                onMessageReceived.onReceived(rabbitConnection.receive(device.getDevice_id()));
            } catch (Exception ex) {
                throw new SpaceBunnyConnectionException(ex);
            }
        }
        throw new SpaceBunnyConnectionException("Space Bunny is not connected. Try spaceBunny.connect().");
    }

    public void subscribe(OnMessageReceivedListener onMessageReceived) throws SpaceBunnyConnectionException { // TODO receive tutti i messaggi
        if (rabbitConnection.isConnected()) {
            try {
                onMessageReceived.onReceived(rabbitConnection.subscribe(device.getDevice_id()));
            } catch (Exception ex) {
                throw new SpaceBunnyConnectionException(ex);
            }
        }
        throw new SpaceBunnyConnectionException("Space Bunny is not connected. Try spaceBunny.connect().");
    }

    private Protocol findProtocol(String p) throws SpaceBunnyConnectionException {
        for (Protocol protocol : device.getProtocols())
            if (protocol.getName().equals(p))
                return protocol;
        throw new SpaceBunnyConnectionException("Standard protocol not found. Try to configure again the device.");
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public ArrayList<Protocol> getProtocols() {
        return device.getProtocols();
    }

    public ArrayList<device.Channel> getChannels() {
        return device.getChannels();
    }

    public void close() throws SpaceBunnyConnectionException {
        if (isConnected()) {
            try {
                rabbitConnection.close();
            } catch (Exception ex) {
                throw new SpaceBunnyConnectionException(ex);
            }
        }
        throw new SpaceBunnyConnectionException("Space Bunny is not connected. Do you have just close the connection?");
    }

    public interface OnFinishConfigiurationListener
    {
        void onConfigured(Device device) throws SpaceBunnyConnectionException;
    }

    public interface OnConnectedListener
    {
        void onConnected() throws SpaceBunnyConnectionException;
    }

    public interface OnMessageReceivedListener
    {
        void onReceived(String message) throws SpaceBunnyConnectionException;
    }

}
