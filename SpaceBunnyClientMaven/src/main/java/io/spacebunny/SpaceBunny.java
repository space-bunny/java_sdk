package io.spacebunny;

<<<<<<< HEAD
=======
import com.rabbitmq.client.ConfirmListener;
>>>>>>> release/Release_0.1.0
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBChannel;
import io.spacebunny.device.SBDevice;
import io.spacebunny.device.SBProtocol;
import io.spacebunny.util.Costants;
import io.spacebunny.util.Utilities;
import org.json.JSONException;
import org.json.JSONObject;

<<<<<<< HEAD
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
=======
import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Map;
>>>>>>> release/Release_0.1.0
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
<<<<<<< HEAD


        /**
         * @param device_key unique key device
         * @constructor
=======
        private boolean custom_certificate = false;


        /**
         *
         *
         * @param device_key unique key device
>>>>>>> release/Release_0.1.0
         */

        public Client(String device_key) throws ConfigurationException {
            if (device_key == null || device_key.equals(""))
                throw new ConfigurationException("Device configuration error.");
            this.device_key = device_key;
        }

        /**
<<<<<<< HEAD
         * @param device custom device created by the user
         * @constructor
=======
         *
         *
         * @param device custom device created by the user
>>>>>>> release/Release_0.1.0
         */
        public Client(SBDevice device) throws ConfigurationException {
            if (device == null)
                throw new ConfigurationException("Device configuration error.");
            this.device = device;
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
<<<<<<< HEAD
         * @throws ConnectionException
=======
         * @throws ConnectionException connection error
>>>>>>> release/Release_0.1.0
         */
        public void connect() throws ConnectionException {
            connect(null, null);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param onConnectedListener callback
<<<<<<< HEAD
         * @throws ConnectionException
=======
         * @throws ConnectionException connection error
>>>>>>> release/Release_0.1.0
         */
        public void connect(io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            connect(null, onConnectedListener);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param protocol            custom protocol defined by the user
         * @param onConnectedListener callback
<<<<<<< HEAD
         * @throws ConnectionException
=======
         * @throws ConnectionException connection error
>>>>>>> release/Release_0.1.0
         */
        public void connect(SBProtocol protocol, io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            try {
                configure();
<<<<<<< HEAD
                if (protocol == null) {
                    protocol = Costants.DEFAULT_PROTOCOL;
                } else {
                    LOGGER.warning("Custom protocol not supported!");
                    protocol = Costants.DEFAULT_PROTOCOL;
                }
=======
                if (protocol != null) {
                    LOGGER.warning("Custom protocol not supported!");
                }
                protocol = Costants.DEFAULT_PROTOCOL;
>>>>>>> release/Release_0.1.0

                rabbitConnection = new RabbitConnection(protocol, tls);
                if (rabbitConnection.connect(device) && onConnectedListener != null)
                    onConnectedListener.onConnected();
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        /**
<<<<<<< HEAD
=======
         *
>>>>>>> release/Release_0.1.0
         * @return connection status
         */
        public boolean isConnected() {
            return rabbitConnection.isConnected();
        }

        /**
         * Test the connection with SpaceBunny
         * Throws an exception if it is not
         *
<<<<<<< HEAD
         * @throws ConnectionException
=======
         * @throws ConnectionException connection error
>>>>>>> release/Release_0.1.0
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
<<<<<<< HEAD
            Utilities.addCA(path);
=======
            addCA(path);
            custom_certificate = true;
>>>>>>> release/Release_0.1.0
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

<<<<<<< HEAD
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
=======
                if (tls) {
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

                        SSLContext sc = SSLContext.getInstance("TLS");
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                    } else {
                        if (!custom_certificate)
                            addCA("certs/lets-encrypt-x3-cross-signed.pem");
                    }
                }

                URL url = new URL(generateHostname(tls));
                uc = url.openConnection();
                uc.setRequestProperty("Device-Key", device_key);
                reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));

                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = reader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                device = new SBDevice(new JSONObject(responseStrBuilder.toString()));

                if (configCallBack != null)
                    configCallBack.onConfigured(this.device);

            } catch (Exception e) {
                throw new SpaceBunny.ConnectionException(e);
            }
>>>>>>> release/Release_0.1.0

        }

        /**
<<<<<<< HEAD
         * @param callBack
=======
         *
         * @param callBack function to do when configuration is finished
>>>>>>> release/Release_0.1.0
         */
        public void setOnFinishConfigiurationListener(io.spacebunny.SpaceBunny.OnFinishConfigiurationListener callBack) {
            configCallBack = callBack;
        }

        /**
         * Publish msg on channel to SpaceBunny
         *
<<<<<<< HEAD
         * @param channelName
         * @param msg     to publish
         * @throws ConnectionException
         */
        public void publish(String channelName, String msg) throws ConnectionException {
            testConnection();
            try {
                SBChannel channel = SBChannel.findChannel(channelName, this.getChannels());
                if (channel != null)
                    rabbitConnection.publish(device.getDevice_id(), channelName, msg);
                else
                    LOGGER.warning("The channel does not exist!");
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
=======
         * @param channelName name of channel where you want to publish
         * @param msg     to publish
         * @throws ConnectionException
         */
        public void publish(final String channelName, final String msg, final Map<String, Object> headers, final ConfirmListener confirmListener) throws ConnectionException {
            testConnection();
            new Thread() {
                public void run() {
                    try {
                        SBChannel channel = SBChannel.findChannel(channelName, device.getChannels());
                        if (channel != null)
                            rabbitConnection.publish(device.getDevice_id(), channelName, msg, headers, confirmListener);
                        else
                            LOGGER.warning("The channel does not exist!");
                    } catch (Exception ex) {
                        LOGGER.warning(ex.getMessage());
                    }
                }
            }.start();

>>>>>>> release/Release_0.1.0
        }

        /**
         * Receive one message from inbox
         *
<<<<<<< HEAD
         * @param onMessageReceived
=======
         * @param onMessageReceived function to do when message is received
>>>>>>> release/Release_0.1.0
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
<<<<<<< HEAD
=======


    private static String generateHostname(boolean tls) {
        return (tls ? Costants.URL_ENDPOINT_TLS : Costants.URL_ENDPOINT) + Costants.API_VERSION + Costants.PATH_ENDPOINT;
    }

    private static void addCA(String path) throws SpaceBunny.ConfigurationException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"),
                    "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath),
                    "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            File f = new File(path);
            if (f.exists()) {
                try (InputStream caInput = new BufferedInputStream(
                        new FileInputStream(path))) {
                    Certificate crt = cf.generateCertificate(caInput);

                    keyStore.setCertificateEntry(f.getName(), crt);
                }

                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLContext.setDefault(sslContext);
            } else {
                throw new SpaceBunny.ConfigurationException("Error with custom CA path.");
            }
        } catch (Exception e) {
            throw new SpaceBunny.ConfigurationException("Error with custom CA.");
        }
    }
>>>>>>> release/Release_0.1.0
}
