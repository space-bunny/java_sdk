package device;

import config.Costants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Device {

    private static String CONNECTION_KEY = "connection";
    private static String CHANNELS_KEY = "channels";
    private static String PROPERTIES_KEY = "properties";
    private static String HOST_KEY = "host";
    private static String PROTOCOLS_KEY = "protocols";
    private static String DEVICE_NAME_KEY = "device_name";
    private static String DEVICE_ID_KEY = "device_id";
    private static String SECRET_KEY = "secret";
    private static String VHOST_KEY = "vhost";

    public ArrayList<Channel> channels = new ArrayList<>();

    private String host;
    private ArrayList<Protocol> protocols = new ArrayList<>(Costants.min_protocols);
    private String device_name;
    private String device_id;
    private String secret;
    private String vhost;

    public Device(JSONObject jsonObject) {
        try {

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

        } catch (Exception ex) {
            ex.printStackTrace();
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
}
