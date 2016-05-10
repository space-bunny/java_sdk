package device;

import org.json.JSONObject;

import java.util.ArrayList;

public class Channel {

    private static String ID_KEY = "id";
    private static String NAME_KEY = "name";

    public String id;
    public String name;

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Channel(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(ID_KEY);
            this.name = jsonObject.getString(NAME_KEY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
