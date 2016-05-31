package io.spacebunny.util;

import io.spacebunny.device.SBProtocol;

public class Constants {

<<<<<<< HEAD
    public final static String URL_ENDPOINT = "http://api.demo.spacebunny.io";
    public final static String URL_ENDPOINT_TLS = "https://api.demo.spacebunny.io";
=======
    public final static String URL_ENDPOINT = "http://api.spacebunny.io";
    public final static String URL_ENDPOINT_TLS = "https://api.spacebunny.io";
>>>>>>> release/Release_0.1.0
    public final static String API_VERSION = "/v1";
    public final static String DEVICE_PATH_ENDPOINT = "/device_configurations";
    public final static String LIVE_STREAM_PATH_ENDPOINT = "/live_stream_key_configurations";


    public static SBProtocol DEFAULT_PROTOCOL = new SBProtocol("amqp", 5672, 5671);
}
