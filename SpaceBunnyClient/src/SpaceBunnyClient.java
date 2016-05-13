/**
 * A module that exports the base SpaceBunny client
 * @module SpaceBunny
 */

// Import some helpers modules
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

import config.Utilities;
import connection.RabbitConnection;
import device.*;
import exception.SpaceBunnyConfigurationException;
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
    private boolean verify_ca = true;


    /**
     *
     * @constructor
     * @param device_key unique key device
     */

    public SpaceBunnyClient(String device_key) throws SpaceBunnyConnectionException {
        if (device_key == null || device_key.equals(""))
            throw new SpaceBunnyConnectionException("Configuration error.");
        this.device_key = device_key;
    }

    /**
     *
     * @constructor
     * @param device custom device created by the user
     */
    public SpaceBunnyClient(Device device) {
        this.device = device;
    }

    /**
     * Open RabbitMQ connection with SpaceBunny
     * @throws SpaceBunnyConnectionException
     */
    public void connect() throws SpaceBunnyConnectionException {
        connect(null, null);
    }

    /**
     * Open RabbitMQ connection with SpaceBunny
     * @param onConnectedListener callback
     * @throws SpaceBunnyConnectionException
     */
    public void connect(OnConnectedListener onConnectedListener) throws SpaceBunnyConnectionException {
        connect(null, onConnectedListener);
    }

    /**
     * Open RabbitMQ connection with SpaceBunny
     * @param protocol custom protocol defined by the user
     * @param onConnectedListener callback
     * @throws SpaceBunnyConnectionException
     */
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

    /**
     *
     * @return connection status
     */
    public boolean isConnected() {
        return rabbitConnection.isConnected();
    }

    /**
     * Test the connection with SpaceBunny
     * Throws an exception if it is not
     * @throws SpaceBunnyConnectionException
     */
    public void testConnection() throws SpaceBunnyConnectionException {
        if (!isConnected())
            throw new SpaceBunnyConnectionException("Space Bunny is not connected. Try spaceBunny.connect().");
    }

    /**
     * Set a custom CA
     * @param path of CA
     */
    public void setPathCustomCA(String path) {
        Utilities.addCA(path);
    }

    /**
     * Download device configuration from SpaceBunny
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws JSONException
     * @throws SpaceBunnyConnectionException
     */
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

            if (!verify_ca) {
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
            } else {
                throw new SpaceBunnyConnectionException("Error with ssl connection!");
            }

        }

        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = reader.readLine()) != null)
            responseStrBuilder.append(inputStr);

        device = new Device(new JSONObject(responseStrBuilder.toString()));

        if (configCallBack != null)
            configCallBack.onConfigured(this.device);

    }

    /**
     *
     * @param callBack
     */
    public void setOnFinishConfigiurationListener(@Nullable OnFinishConfigiurationListener callBack) {
        configCallBack = callBack;
    }

    /**
     * Publish msg on channel to SpaceBunny
     * @param channel
     * @param msg to publish
     * @throws SpaceBunnyConnectionException
     */
    public void publish(device.Channel channel, String msg) throws SpaceBunnyConnectionException {
        testConnection();
        try {
            rabbitConnection.publish(device.getDevice_id(), channel, msg);
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
    }

    /**
     * Receive one message from inbox
     * @param onMessageReceived
     * @throws SpaceBunnyConnectionException
     */
    public void receive(OnMessageReceivedListener onMessageReceived) throws SpaceBunnyConnectionException {
        testConnection();
        try {
            onMessageReceived.onReceived(rabbitConnection.receive(device.getDevice_id()));
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
    }

    /**
     * Subscribe to input channel
     * @param onMessageReceived callBack on message received
     * @throws SpaceBunnyConnectionException
     */
    public void subscribe(RabbitConnection.OnSubscriptionMessageReceivedListener onMessageReceived) throws SpaceBunnyConnectionException {
        testConnection();
        try {
            rabbitConnection.subscribe(device.getDevice_id(), onMessageReceived);
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
    }

    /**
     * Unsubscribe to input channel
     * @throws SpaceBunnyConnectionException
     */
    public void unsubscribe() throws SpaceBunnyConnectionException {
        testConnection();
        try {
            rabbitConnection.unsubscribe(device.getDevice_id());
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
    }

    /**
     * Find protocol by his name
     * @param p
     * @return searched protocol
     * @throws SpaceBunnyConfigurationException
     */
    private Protocol findProtocol(String p) throws SpaceBunnyConfigurationException {
        for (Protocol protocol : device.getProtocols())
            if (protocol.getName().equals(p))
                return protocol;
        throw new SpaceBunnyConfigurationException("Standard protocol not found. Try to configure again the device.");
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setVerifyCA(boolean verify) {
        this.verify_ca = verify;
    }

    public boolean isCAVerifed() {
        return this.verify_ca;
    }

    public ArrayList<Protocol> getProtocols() {
        return device.getProtocols();
    }

    public ArrayList<device.Channel> getChannels() {
        return device.getChannels();
    }

    /**
     * Close connection to SpaceBunny
     * @throws SpaceBunnyConnectionException
     */
    public void close() throws SpaceBunnyConnectionException {
        testConnection();
        try {
            rabbitConnection.close();
        } catch (Exception ex) {
            throw new SpaceBunnyConnectionException(ex);
        }
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
