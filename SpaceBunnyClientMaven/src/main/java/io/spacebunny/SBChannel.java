package io.spacebunny;

/**
 * A module that exports a channel
 * @module SBChannel
 */

import org.json.JSONException;
import org.json.JSONObject;

public class SBChannel {

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
}