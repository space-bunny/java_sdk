package io.spacebunny.util;

import io.spacebunny.device.SBProtocol;

public class Costants {

    public final static String URL_ENDPOINT = "http://api.demo.spacebunny.io";
    public final static String URL_ENDPOINT_TLS = "https://api.demo.spacebunny.io";
    public final static String API_VERSION = "/v1";
    public final static String PATH_ENDPOINT = "/device_configurations";

    public static SBProtocol DEFAULT_PROTOCOL = new SBProtocol("amqp", 5672, 5671);
}
