package io.spacebunny.device;

import io.spacebunny.SpaceBunny;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public class SBLiveStream {
    private final static Logger LOGGER = Logger.getLogger(SpaceBunny.class.getName());

    private final static String ID_KEY = "id";
    private final static String NAME_KEY = "name";

    private String id;
    private String name;

    public SBLiveStream(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public SBLiveStream(JSONObject jsonObject) {
        this.id = jsonObject.getString(ID_KEY);
        this.name = jsonObject.getString(NAME_KEY);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static SBLiveStream findLiveStream(String name, ArrayList<SBLiveStream> liveStreams) throws SpaceBunny.ConnectionException {
        if (liveStreams != null)
            for (SBLiveStream stream : liveStreams)
                if (stream.getName().equals(name))
                    return stream;
        throw new SpaceBunny.ConnectionException("Live Stream not exists.");
    }
}
