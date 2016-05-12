package device;

/**
 * A module that exports a protocol
 * @module Protocol
 */


import org.json.JSONException;
import org.json.JSONObject;

public class Protocol {

    private final static String PORT_KEY = "port";
    private final static String SSL_PORT_KEY = "ssl_port";

    private String name;
    private int port;
    private int ssl_port;

    /**
     *
     * @constructor
     * @param name of the protocol
     * @param port of the protocol
     * @param ssl_port of the protocol for secure connection
     */

    public Protocol(String name, int port, int ssl_port) {
        this.name = name;
        this.port = port;
        this.ssl_port = ssl_port;
    }

    /**
     *
     * @constructor
     * @param name of the protocol
     * @param js JSONObject that contains all the information about the protocol
     */

    public Protocol(String name, JSONObject js) throws JSONException {
        this.name = name;
        this.port = js.getInt(PORT_KEY);
        this.ssl_port = js.getInt(SSL_PORT_KEY);
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
     * @return ssl_port of the protocol
     */
    public int getSsl_port() {
        return ssl_port;
    }

    /**
     *
     * @return string with protocol information
     */
    public String toString() {
        return "\nPROTOCOL:\nNAME: " + name + "\nPORT: " + port + "\nSSL_PORT: " + ssl_port;
    }
}
