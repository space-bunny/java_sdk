package device;

import config.Costants;
import exception.SpaceBunnyConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Device {

    private final static String CONNECTION_KEY = "connection";
    private final static String CHANNELS_KEY = "channels";
    private final static String PROPERTIES_KEY = "properties";
    private final static String HOST_KEY = "host";
    private final static String PROTOCOLS_KEY = "protocols";
    private final static String DEVICE_NAME_KEY = "device_name";
    private final static String DEVICE_ID_KEY = "device_id";
    private final static String SECRET_KEY = "secret";
    private final static String VHOST_KEY = "vhost";

    private String host;
    private ArrayList<Protocol> protocols = new ArrayList<>(Costants.min_protocols);
    private ArrayList<Channel> channels = new ArrayList<>();
    private String device_name;
    private String device_id;
    private String secret;
    private String vhost;

    public Device(String device_name, String device_id, String secret, ArrayList<Protocol> protocols, ArrayList<Channel> channels, String host, String vhost) throws SpaceBunnyConfigurationException {
        this.host = host;
        this.protocols = protocols;
        this.device_name =  device_name; // TODO compatibilità 1.6 java
        this.device_id = device_id;
        this.secret =  secret;
        this.vhost =  vhost;
        this.channels = channels;

        if (this.host == null ||
                this.host.equals("") ||
                this.protocols == null ||
                this.protocols.size() == 0 ||
                this.device_name == null ||
                this.device_name.equals("") ||
                this.device_id == null ||
                this.device_id.equals("") ||
                this.secret == null ||
                this.secret.equals("") ||
                this.vhost == null ||
                this.vhost.equals("") ||
                this.channels == null ||
                this.channels.size() == 0)
            throw new SpaceBunnyConfigurationException("Error Device Configuration");

    }

    public Device(JSONObject jsonObject) throws JSONException {
            // Connection
            JSONObject conn = jsonObject.getJSONObject(CONNECTION_KEY);
            this.host = conn.getString(HOST_KEY);

            JSONObject pr = conn.getJSONObject(PROTOCOLS_KEY);
            Iterator<?> keys = pr.keys();

            while( keys.hasNext() ) {
                String key = (String)keys.next();
                this.protocols.add(new Protocol(key, pr.getJSONObject(key)));
            }
            this.device_name =  conn.getString(DEVICE_NAME_KEY);
            this.device_id =  conn.getString(DEVICE_ID_KEY);
            this.secret =  conn.getString(SECRET_KEY);
            this.vhost =  conn.getString(VHOST_KEY);

            // Channels
            JSONArray ch = jsonObject.getJSONArray(CHANNELS_KEY);
            for (int i = 0; i < ch.length(); i++) {
                channels.add(new Channel(ch.getJSONObject(i)));
            }
    }

    public ArrayList<Protocol> getProtocols() {
        return protocols;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public String getHost() {
        return host;
    }

    public String getDevice_name() {
        return device_name;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getSecret() {
        return secret;
    }

    public String getVhost() {
        return vhost;
    }

    public String toString() {
        return "\nHOST: " + host + "\nPROTOCOLS: " + protocols.toString() +  "\nDEVICE NAME: " + device_name + "\nDEVICE_ID: " + device_id + "\nSECRET: " + secret + "\nVHOST: " + vhost
        + "\n" + channels.toString();
    }

    public static class DeviceBuilder{

        private String host;
        private ArrayList<Protocol> protocols = new ArrayList<>(Costants.min_protocols);
        private ArrayList<Channel> channels = new ArrayList<>();
        private String device_name;
        private String device_id;
        private String secret;
        private String vhost;

        public DeviceBuilder() {
        }

        public DeviceBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public DeviceBuilder setDeviceName(String device_name) {
            this.device_name = device_name;
            return this;
        }

        public DeviceBuilder setDeviceId(String device_id) {
            this.device_id = device_id;
            return this;
        }

        public DeviceBuilder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public DeviceBuilder setVHost(String vhost) {
            this.vhost = vhost;
            return this;
        }

        public DeviceBuilder setSecret(ArrayList<Protocol> protocols) {
            this.protocols = protocols;
            return this;
        }

        public DeviceBuilder setVHost(ArrayList<Channel> channels) {
            this.channels = channels;
            return this;
        }

        public Device getDevice() throws SpaceBunnyConfigurationException {
            return new Device(device_name, device_id, secret, protocols, channels, host, vhost);
        }
    }
}

