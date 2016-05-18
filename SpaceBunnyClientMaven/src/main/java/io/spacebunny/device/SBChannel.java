package io.spacebunny.device;

/**
 * A module that exports a channel
 * @module SBChannel
 */

import io.spacebunny.SpaceBunny;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public class SBChannel {


    private final static Logger LOGGER = Logger.getLogger(SpaceBunny.class.getName());

    private final static String ID_KEY = "id";
    private final static String NAME_KEY = "name";

    private String id;
    private String name;

    /**
     *
     * @constructor
     * @param id of the channel
     * @param name of the channel
     */

    public SBChannel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     *
     * @constructor
     * @param jsonObject that contains all the information about the channel
     */

    public SBChannel(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString(ID_KEY);
        this.name = jsonObject.getString(NAME_KEY);
    }

    /**
     *
     * @return id of the channel
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return name of the channel
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return string with channel information
     */
    public String toString() {
        return "\nChannel:\nID: " + id + "\nNAME: " + name;
    }

    public static SBChannel findChannel(String name, ArrayList<SBChannel> channels) throws SpaceBunny.ConfigurationException {
        for (SBChannel channel : channels)
            if (channel.getName().equals(name))
                return channel;
        return null;
    }
}
