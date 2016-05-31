package io.spacebunny;

import com.rabbitmq.client.ConfirmListener;
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBChannel;
import io.spacebunny.device.SBDevice;
import io.spacebunny.device.SBLiveStream;
import io.spacebunny.device.SBProtocol;
import io.spacebunny.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Iterator;
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
        private boolean custom_certificate = false;


        /**
         *
         *
         * @param device_key unique key device
         */

        public Client(String device_key) throws ConfigurationException {
            if (device_key == null || device_key.equals(""))
                throw new ConfigurationException("Device configuration error.");
            this.device_key = device_key;
        }

        /**
         *
         *
         * @param device custom device created by the user
         */
        public Client(SBDevice device) throws ConfigurationException {
            if (device == null)
                throw new ConfigurationException("Device configuration error.");
            this.device = device;
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @throws ConnectionException connection error
         */
        public void connect() throws ConnectionException {
            connect(null, null);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param onConnectedListener callback
         * @throws ConnectionException connection error
         */
        public void connect(io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            connect(null, onConnectedListener);
        }

        /**
         * Open RabbitMQ connection with SpaceBunny
         *
         * @param protocol            custom protocol defined by the user
         * @param onConnectedListener callback
         * @throws ConnectionException connection error
         */
        public void connect(SBProtocol protocol, io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws ConnectionException {
            try {
                configure();
                if (protocol != null) {
                    LOGGER.warning("Custom protocol not supported!");
                }
                protocol = Constants.DEFAULT_PROTOCOL;

                rabbitConnection = new RabbitConnection(protocol, tls);
                if (rabbitConnection.connect(device) && onConnectedListener != null)
                    onConnectedListener.onConnected();
            } catch (Exception ex) {
                throw new ConnectionException(ex);
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
         *
         * @throws ConnectionException connection error
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
            addCA(path);
            custom_certificate = true;
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
                        if (!custom_certificate) {
                            setDefaultCA();
                        }
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

        }

        /**
         *
         * @param callBack function to do when configuration is finished
         */
        public void setOnFinishConfigiurationListener(io.spacebunny.SpaceBunny.OnFinishConfigiurationListener callBack) {
            configCallBack = callBack;
        }

        /**
         * Publish msg on channel to SpaceBunny
         *
         * @param channelName name of channel where you want to publish
         * @param msg     to publish
         * @throws ConnectionException
         */
        public void publish(final String channelName, final String msg, final Map<String, Object> headers, final ConfirmListener confirmListener) throws ConnectionException {
            testConnection();
            new Thread() {
                public void run() {
                    try {
                        SBChannel channel = SBChannel.findChannel(channelName, device);
                        rabbitConnection.publish(device.getDevice_id(), channelName, msg, headers, confirmListener);
                        if (channel == null)
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
         * @param onMessageReceived function to do when message is received
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
                ex.printStackTrace();
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

        public SBDevice getDevice() {
            return device;
        }

        public SBProtocol getDefaultProtocol() {
            return Constants.DEFAULT_PROTOCOL;
        }

        public boolean existsChannel(String channelName) {
            return (SBChannel.findChannel(channelName, this.device) != null);
        }
    }

    public static class LiveStream {
        private final static String CONNECTION_KEY = "connection";
        private final static String HOST_KEY = "host";
        private final static String PROTOCOLS_KEY = "protocols";
        private final static String LIVE_STREAMS_KEY = "live_streams";
        private final static String CLIENT_KEY = "client";
        private final static String SECRET_KEY = "secret";
        private final static String VHOST_KEY = "vhost";

        private String host;
        private ArrayList<SBProtocol> protocols = new ArrayList<>();
        private String client;
        private String secret;
        private String vhost;
        private ArrayList<SBLiveStream> liveStreams = new ArrayList<>();

        private boolean tls = true;
        private boolean verify_ca = true;
        private RabbitConnection rabbitConnection = null;
        private boolean custom_certificate = false;

        private String liveStream_key_client;
        private String liveStream_key_secret;


        public LiveStream(String liveStream_key_client, String liveStream_key_secret) throws ConfigurationException {
            if (liveStream_key_client == null || liveStream_key_client.equals("") ||
                    liveStream_key_secret == null || liveStream_key_secret.equals(""))
                throw new ConfigurationException("Live Stream configuration error.");
            this.liveStream_key_client = liveStream_key_client;
            this.liveStream_key_secret = liveStream_key_secret;
        }

        public void connect(io.spacebunny.SpaceBunny.OnConnectedListener onConnectedListener) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException, ConnectionException {

            URLConnection uc;
            BufferedReader reader;

            try {

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
                        if (!custom_certificate) {
                            setDefaultCA();
                        }
                    }
                }

                URL url = new URL(generateLiveStreamHostname(tls));
                uc = url.openConnection();
                uc.setRequestProperty("Live-Stream-Key-Client", liveStream_key_client);
                uc.setRequestProperty("Live-Stream-Key-Secret", liveStream_key_secret);
                reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));

                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = reader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

                JSONObject conn = jsonObject.getJSONObject(CONNECTION_KEY);
                host = conn.getString(HOST_KEY);

                JSONObject pr = conn.getJSONObject(PROTOCOLS_KEY);
                Iterator<?> keys = pr.keys();

                protocols.add(Constants.DEFAULT_PROTOCOL);

                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    SBProtocol newProtocol = new SBProtocol(key, pr.getJSONObject(key));

                    // Check default protocol updates
                    if (newProtocol.getName().equals(Constants.DEFAULT_PROTOCOL.getName()))
                        protocols.remove(Constants.DEFAULT_PROTOCOL);

                    protocols.add(newProtocol);
                }

                JSONArray ls = jsonObject.getJSONArray(LIVE_STREAMS_KEY);
                for(Object obg : ls) {
                    liveStreams.add(new SBLiveStream((JSONObject) obg));
                }

                this.client =  conn.getString(CLIENT_KEY);
                this.secret =  conn.getString(SECRET_KEY);
                this.vhost =  conn.getString(VHOST_KEY);

                // Rabbit Connection
                SBProtocol protocol = Constants.DEFAULT_PROTOCOL;

                rabbitConnection = new RabbitConnection(protocol, tls);
                if (rabbitConnection.connect(host, vhost, client, secret) && onConnectedListener != null)
                    onConnectedListener.onConnected();

            } catch (Exception e) {
                throw new SpaceBunny.ConnectionException(e);
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
         *
         * @throws ConnectionException connection error
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
            addCA(path);
            custom_certificate = true;
        }

        public void subscribe(String liveStreamName, boolean cache, RabbitConnection.OnSubscriptionMessageReceivedListener onMessageReceived) throws ConnectionException {
            testConnection();
            SBLiveStream liveStream = SBLiveStream.findLiveStream(liveStreamName, liveStreams);
            try {
                rabbitConnection.subscribeLiveStream(liveStream, cache, client, onMessageReceived);
            } catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }

        public void unsubscribe(String liveStreamName, boolean cache) throws ConnectionException {
            testConnection();
            SBLiveStream liveStream = SBLiveStream.findLiveStream(liveStreamName, liveStreams);
            try {
                rabbitConnection.unsubscribeLiveStream(liveStream, cache, client);
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
            return protocols;
        }

        public ArrayList<SBLiveStream> getLiveStreams() {
            return liveStreams;
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

        public SBProtocol getDefaultProtocol() {
            return Constants.DEFAULT_PROTOCOL;
        }

        public boolean existsLiveStream(String liveStreamName) {
            try {
                SBLiveStream.findLiveStream(liveStreamName, liveStreams);
                return true;
            } catch (ConnectionException ex) {
                return false;
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


    private static String generateHostname(boolean tls) {
        return (tls ? Constants.URL_ENDPOINT_TLS : Constants.URL_ENDPOINT) + Constants.API_VERSION + Constants.DEVICE_PATH_ENDPOINT;
    }

    private static String generateLiveStreamHostname(boolean tls) {
        return (tls ? Constants.URL_ENDPOINT_TLS : Constants.URL_ENDPOINT) + Constants.API_VERSION + Constants.LIVE_STREAM_PATH_ENDPOINT;
    }

    private static void addCA(String path) throws SpaceBunny.ConfigurationException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String ksPath = System.getProperty("java.home") + "\\lib\\security\\cacerts\\";

            String vendor = System.getProperty("java.vendor.url");
            if (vendor.equals("http://www.android.com/"))
                keyStore.load(null, null);
            else
                keyStore.load(new FileInputStream(ksPath),
                    "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            File f = new File(URLDecoder.decode(path, "UTF-8"));
            if (f.exists()) {
                try (InputStream caInput = new BufferedInputStream(
                        new FileInputStream(f.getPath()))) {
                    Certificate crt = cf.generateCertificate(caInput);

                    keyStore.setCertificateEntry(f.getName(), crt);

                    TrustManagerFactory tmf = TrustManagerFactory
                            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keyStore);
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, tmf.getTrustManagers(), null);
                    SSLContext.setDefault(sslContext);
                }
            } else {
                throw new SpaceBunny.ConfigurationException("Error with custom CA path.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new SpaceBunny.ConfigurationException("Error with custom CA.");
        }
    }

    private static void setDefaultCA() throws SpaceBunny.ConfigurationException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String ksPath = System.getProperty("java.home") + "\\lib\\security\\cacerts\\";

            String vendor = System.getProperty("java.vendor.url");
            if (vendor.equals("http://www.android.com/"))
                keyStore.load(null, null);
            else
                keyStore.load(new FileInputStream(ksPath),
                        "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            try (InputStream caInput = new BufferedInputStream(
                    SpaceBunny.class.getResourceAsStream("/lets-encrypt-x3-cross-signed.pem"))) {
                Certificate crt = cf.generateCertificate(caInput);

                keyStore.setCertificateEntry("lets-encrypt-x3-cross-signed", crt);

                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLContext.setDefault(sslContext);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new SpaceBunny.ConfigurationException("Error with custom CA.");
        }
    }
}
