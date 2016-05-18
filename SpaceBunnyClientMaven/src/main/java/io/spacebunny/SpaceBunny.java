package io.spacebunny;

import com.rabbitmq.client.ConfirmListener;
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBChannel;
import io.spacebunny.device.SBDevice;
import io.spacebunny.device.SBProtocol;
import io.spacebunny.util.Costants;
import io.spacebunny.util.Utilities;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class SpaceBunny {

    private final static Logger LOGGER = Logger.getLogger(SpaceBunny.class.getName());

    public static class Client {

        private SBDevice device = null;
        private RabbitConnection rabbitConnection = null;

        private OnFinishConfigiurationListener configCallBack = null;

        private String device_key;
        private boolean tls = true;
        private boolean verify_ca = true;


        /**
         * @param device_key unique key device
         * @constructor
         */

        public Client(String device_key) throws ConfigurationException {
            if (device_key == null || device_key.equals(""))
                throw new ConfigurationException("Device configuration error.");
            this.device_key = device_key;
        }

        /**
         * @param device custom device created by the user
         * @constructor
         */
        public Client(SBDevice device) throws ConfigurationException {
            if (device == null)
                throw new ConfigurationException("Device configuration error.");
            this.device = device;
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @throws ConnectionException
         */
        public void connect() throws ConnectionException {
            connect(null, null);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param onConnectedListener callback
         * @throws ConnectionException
         */
        public void connect(io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            connect(null, onConnectedListener);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param protocol            custom protocol defined by the user
         * @param onConnectedListener callback
         * @throws ConnectionException
         */
        public void connect(SBProtocol protocol, io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            try {
                configure();
                if (protocol == null) {
                    protocol = Costants.DEFAULT_PROTOCOL;
                } else {
                    LOGGER.warning("Custom protocol not supported!");
                    protocol = Costants.DEFAULT_PROTOCOL;
                }

                rabbitConnection = new RabbitConnection(protocol, tls);
                if (rabbitConnection.connect(device) && onConnectedListener != null)
                    onConnectedListener.onConnected();
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        /**
         * @return connection status
         */
        public boolean isConnected() {
            return rabbitConnection.isConnected();
        }

        /**
         * Test the connection with SpaceBunny
         * Throws an exception if it is not
         *
         * @throws ConnectionException
         */
        public void testConnection() throws ConnectionException {
            if (!isConnected())
                throw new ConnectionException("Space Bunny is not connected. Try spaceBunny.connect().");
        }

        /**
         * Set a custom CA
         *
         * @param path of CA
         */
        public void setPathCustomCA(String path) throws ConfigurationException {
            Utilities.addCA(path);
        }

        /**
         * Download device configuration from SpaceBunny
         *
         * @throws KeyManagementException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws JSONException
         * @throws ConnectionException
         */
        private void configure() throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException, ConnectionException {

            URLConnection uc;
            BufferedReader reader;

            try {

                URL url = new URL(Utilities.generateHostname(tls));
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


                    URL url = new URL(Utilities.generateHostname(tls));
                    uc = url.openConnection();
                    uc.setRequestProperty("Device-Key", device_key);
                    reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
                } else {
                    throw new ConnectionException("Error with ssl connection!");
                }

            }

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = reader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            device = new SBDevice(new JSONObject(responseStrBuilder.toString()));

            if (configCallBack != null)
                configCallBack.onConfigured(this.device);

        }

        /**
         * @param callBack
         */
        public void setOnFinishConfigiurationListener(io.spacebunny.SpaceBunny.OnFinishConfigiurationListener callBack) {
            configCallBack = callBack;
        }

        /**
         * Publish msg on channel to SpaceBunny
         *
         * @param channelName
         * @param msg     to publish
         * @throws ConnectionException
         */
        public void publish(final String channelName, final String msg, final Map<String, Object> headers, ConfirmListener confirmListener) throws ConnectionException {
            testConnection();
            new Thread() {
                public void run() {
                    try {
                        SBChannel channel = SBChannel.findChannel(channelName, device.getChannels());
                        if (channel != null)
                            rabbitConnection.publish(device.getDevice_id(), channelName, msg, headers, null);
                        else
                            LOGGER.warning("The channel does not exist!");
                    } catch (Exception ex) {
                        LOGGER.warning(ex.getMessage());
                    }
                }
            }.start();

        }

        /**
         * Receive one message from inbox
         *
         * @param onMessageReceived
         * @throws ConnectionException
         */
        public void receive(io.spacebunny.SpaceBunny.OnMessageReceivedListener onMessageReceived) throws ConnectionException {
            testConnection();
            try {
                onMessageReceived.onReceived(rabbitConnection.receive(device.getDevice_id()));
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        /**
         * Subscribe to input channel
         *
         * @param onMessageReceived callBack on message received
         * @throws ConnectionException
         */
        public void subscribe(RabbitConnection.OnSubscriptionMessageReceivedListener onMessageReceived) throws ConnectionException {
            testConnection();
            try {
                rabbitConnection.subscribe(device.getDevice_id(), onMessageReceived);
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        /**
         * Unsubscribe to input channel
         *
         * @throws ConnectionException
         */
        public void unsubscribe() throws ConnectionException {
            testConnection();
            try {
                rabbitConnection.unsubscribe(device.getDevice_id());
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        public void setTls(boolean tls) {
            this.tls = tls;
        }

        public boolean istls() {
            return this.tls;
        }

        public void setVerifyCA(boolean verify) {
            this.verify_ca = verify;
        }

        public boolean isCAVerifed() {
            return this.verify_ca;
        }

        public ArrayList<SBProtocol> getProtocols() {
            return device.getProtocols();
        }

        public ArrayList<SBChannel> getChannels() {
            return device.getChannels();
        }

        /**
         * Close connection to SpaceBunny
         *
         * @throws ConnectionException
         */
        public void close() throws ConnectionException {
            testConnection();
            try {
                rabbitConnection.close();
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }
    }

    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message){
            super(message);
        }

        public ConfigurationException(Exception ex){
            super(ex.getMessage());
        }
    }

    public static class ConnectionException extends Exception {
        public ConnectionException(String message){
            super(message);
        }

        public ConnectionException(Exception ex){
            super(ex.getMessage());
        }
    }

    public interface OnFinishConfigiurationListener
    {
        void onConfigured(SBDevice device) throws ConnectionException;
    }

    public interface OnConnectedListener
    {
        void onConnected() throws ConnectionException;
    }

    public interface OnMessageReceivedListener
    {
        void onReceived(String message) throws ConnectionException;
    }
}
