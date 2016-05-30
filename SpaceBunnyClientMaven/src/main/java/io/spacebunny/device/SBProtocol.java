package io.spacebunny.device;

/**
 * A module that exports a protocol
 * @module SBProtocol
 */


import io.spacebunny.SpaceBunny;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SBProtocol {

    private final static String PORT_KEY = "port";
    private final static String TLS_PORT_KEY = "tls_port";

    private String name;
    private int port;
    private int tls_port;

    /**
     *
     * @param name of the protocol
     * @param port of the protocol
     * @param tls_port of the protocol for secure connection
     */

    public SBProtocol(String name, int port, int tls_port) {
        this.name = name;
        this.port = port;
        this.tls_port = tls_port;
    }

    /**
     *
     * @param name of the protocol
     * @param js JSONObject that contains all the information about the protocol
     */

    public SBProtocol(String name, JSONObject js) throws JSONException {
        this.name = name;
        this.port = js.getInt(PORT_KEY);
        this.tls_port = js.getInt(TLS_PORT_KEY);
    }


    /**
     *
     * @return name of the protocol
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return port of the protocol
     */
    public int getPort() {
        return port;
    }

    /**
     *
     * @return tls_port of the protocol
     */
    public int getTls_port() {
        return tls_port;
    }

    /**
     *
     * @return string with protocol information
     */
    public String toString() {
        return "\nPROTOCOL:\nNAME: " + name + "\nPORT: " + port + "\nTLS_PORT: " + tls_port;
    }



    /**
     * Find protocol by his name
     * @param name of the protocol to search
     * @return searched protocol
     */
    public static SBProtocol findProtocol(String name, SBDevice device) {
        for (SBProtocol protocol : device.getProtocols())
            if (protocol.getName().equals(name))
                return protocol;
        return null;
    }
}
