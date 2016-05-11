package config;

public class Costants {

    public final static String url_endpoint = "http://api.demo.spacebunny.io";
    public final static String url_endpoint_ssl = "https://api.demo.spacebunny.io";
    public final static String api_version = "/v1";
    public final static String path_endpoint = "/device_configurations";
    public final static int min_protocols = 4;

    public static String DEFAULT_PROTOCOL = "amqp";


    public static String generateHostname(boolean ssl) {
        if (ssl)
            return url_endpoint_ssl + api_version + path_endpoint;
        else
            return url_endpoint + api_version + path_endpoint;
    }
}
