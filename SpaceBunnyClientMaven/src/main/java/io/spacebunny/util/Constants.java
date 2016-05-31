package io.spacebunny.util;

import io.spacebunny.device.SBProtocol;

public class Constants {

    public final static String URL_ENDPOINT = "http://api.spacebunny.io";
    public final static String URL_ENDPOINT_TLS = "https://api.spacebunny.io";
    public final static String API_VERSION = "/v1";
    public final static String DEVICE_PATH_ENDPOINT = "/device_configurations";
    public final static String LIVE_STREAM_PATH_ENDPOINT = "/live_stream_key_configurations";


    public static SBProtocol DEFAULT_PROTOCOL = new SBProtocol("amqp", 5672, 5671);
}
