package config;

public class Costants {

    public static String url_endpoint = "http://api.demo.spacebunny.io";
    public static String api_version = "/v1";
    public static String path_endpoint = "/device_configurations";
    public static int min_protocols = 4;

    public static String generateHostname() {
        return url_endpoint + api_version + path_endpoint;
    }
}
