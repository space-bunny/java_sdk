package device;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Channel {

    private final static String ID_KEY = "id";
    private final static String NAME_KEY = "name";

    public String id;
    public String name;

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Channel(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString(ID_KEY);
        this.name = jsonObject.getString(NAME_KEY);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "\nChannel:\nID: " + id + "\nNAME: " + name;
    }
}
