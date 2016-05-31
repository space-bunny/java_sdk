package io.spacebunny.device;


import io.spacebunny.SpaceBunny;
import io.spacebunny.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class SBDevice {

    private final static String CONNECTION_KEY = "connection";
    private final static String CHANNELS_KEY = "channels";
    private final static String HOST_KEY = "host";
    private final static String PROTOCOLS_KEY = "protocols";
    private final static String DEVICE_NAME_KEY = "device_name";
    private final static String DEVICE_ID_KEY = "device_id";
    private final static String SECRET_KEY = "secret";
    private final static String VHOST_KEY = "vhost";
    private final static Logger LOGGER = Logger.getLogger(SpaceBunny.class.getName());

    private String host;
    private ArrayList<SBProtocol> protocols = new ArrayList<>(1);
    private ArrayList<SBChannel> channels = new ArrayList<>();
    private String device_name;
    private String device_id;
    private String secret;
    private String vhost;

    /**
     *

     * @param device_name name of the device
     * @param device_id id of the device
     * @param secret secret password for the device connection
     * @param protocols all protocols available for device
     * @param channels all channels available for device
     * @param host to which you must connect
     * @param vhost to which you must connect
     * @throws SpaceBunny.ConfigurationException configuration error
     */

    public SBDevice(String device_name, String device_id, String secret, ArrayList<SBProtocol> protocols, ArrayList<SBChannel> channels, String host, String vhost) throws SpaceBunny.ConfigurationException {
        this.host = host;
        this.protocols = protocols;
        this.device_name =  device_name;
        this.device_id = device_id;
        this.secret =  secret;
        this.vhost =  vhost;
        this.channels = channels;

        // Check if default protocol already exists
        if (SBProtocol.findProtocol(Constants.DEFAULT_PROTOCOL.getName(), this) == null)
            this.protocols.add(0, Constants.DEFAULT_PROTOCOL);

        if (this.host == null ||
                this.host.equals("") ||
                this.device_name == null ||
                this.device_name.equals("") ||
                this.device_id == null ||
                this.device_id.equals("") ||
                this.secret == null ||
                this.secret.equals("") ||
                this.vhost == null ||
                this.vhost.equals(""))
            throw new SpaceBunny.ConfigurationException("Error in Device Configuration!");

        if (this.channels.size() == 0)
            LOGGER.warning("No channel has been added.");

    }

    /**
     *
     * @param jsonObject json object with all information
     * @throws JSONException json read error
     */
    public SBDevice(JSONObject jsonObject) throws JSONException {
        // Connection
        JSONObject conn = jsonObject.getJSONObject(CONNECTION_KEY);
        this.host = conn.getString(HOST_KEY);

        JSONObject pr = conn.getJSONObject(PROTOCOLS_KEY);
        Iterator<?> keys = pr.keys();

        this.protocols.add(Constants.DEFAULT_PROTOCOL);

        while( keys.hasNext() ) {
            String key = (String)keys.next();
            SBProtocol newProtocol = new SBProtocol(key, pr.getJSONObject(key));

            // Check default protocol updates
            if (newProtocol.getName().equals(Constants.DEFAULT_PROTOCOL.getName()))
                this.protocols.remove(Constants.DEFAULT_PROTOCOL);

            this.protocols.add(newProtocol);
        }
        this.device_name =  conn.getString(DEVICE_NAME_KEY);
        this.device_id =  conn.getString(DEVICE_ID_KEY);
        this.secret =  conn.getString(SECRET_KEY);
        this.vhost =  conn.getString(VHOST_KEY);

        // Channels
        JSONArray ch = jsonObject.getJSONArray(CHANNELS_KEY);
        for (int i = 0; i < ch.length(); i++) {
            channels.add(new SBChannel(ch.getJSONObject(i)));
        }
    }

    public ArrayList<SBProtocol> getProtocols() {
        return protocols;
    }

    public ArrayList<SBChannel> getChannels() {
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

    /**
     * Device Custom Builder
     */
    public static class Builder{

        private String host;
        private ArrayList<SBProtocol> protocols = new ArrayList<>(1);
        private ArrayList<SBChannel> channels = new ArrayList<>();
        private String device_name;
        private String device_id;
        private String secret;
        private String vhost;

        public Builder() {
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setDeviceName(String device_name) {
            this.device_name = device_name;
            return this;
        }

        public Builder setDeviceId(String device_id) {
            this.device_id = device_id;
            return this;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder setVHost(String vhost) {
            this.vhost = vhost;
            return this;
        }

        public Builder setProtocols(ArrayList<SBProtocol> protocols) {
            this.protocols = protocols;
            return this;
        }

        public Builder setChannels(ArrayList<SBChannel> channels) {
            this.channels = channels;
            return this;
        }

        public SBDevice getDevice() throws SpaceBunny.ConfigurationException {
            return new SBDevice(device_name, device_id, secret, protocols, channels, host, vhost);
        }
    }
}

