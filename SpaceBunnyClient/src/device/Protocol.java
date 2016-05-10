package device;

import org.json.JSONObject;

public class Protocol {

    private static String PORT_KEY = "port";
    private static String SSL_PORT_KEY = "ssl_port";

    private String name;
    private int port;
    private int ssl_port;

    public Protocol(String name, int port, int ss_port) {
        this.name = name;
        this.port = port;
        this.ssl_port = ssl_port;
    }

    public Protocol(String name, JSONObject js) {
        try {
            this.name = name;
            this.port = js.getInt(PORT_KEY);
            this.ssl_port = js.getInt(SSL_PORT_KEY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getSsl_port() {
        return ssl_port;
    }

    public String toString() {
        return "\nPROTOCOL:\nNAME: " + name + "\nPORT: " + port + "\nSSL_PORT: " + ssl_port;
    }
}
