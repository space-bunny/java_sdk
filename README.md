<p align="center">
  <img width="480" src="assets/logo.png"/>
</p>

[Space Bunny](http://spacebunny.io) is the IoT platform that makes it easy for you and your devices to send and exchange messages with a server or even with each other. You can store the data, receive timely event notifications, monitor live streams and remotely control your devices. Easy to use, and ready to scale at any time.

This is the source code repository for Java SDK.
Please feel free to contribute!

## Installation

Gradle: 

```
compile 'io.spacebunny:device-sdk:0.0.1'
```

Maven: 

```
<dependency>
    <groupId>io.spacebunny</groupId>
    <artifactId>device-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

Download library: [Jar file](http://linkonsdk.github.io)

## Device basic usage

Devices can publish messages on configured channels and receive messages on their `inbox` channel

#### Configuration

Configure the instance of the SpaceBunny's Client with a valid Device Key, or with a custom device:

```java
try {
    final SpaceBunny.Client spaceBunny = new SpaceBunny.Client("your_device_key");
} catch (SpaceBunny.ConfigurationException ex) {
    ex.printStackTrace();
}
```

Build an instance of a device, configure and use it:

```java
try {
    String host = "host_to_connect";
    String device_name = "name_of_the_device";
    String device_id = "device_identifer";
    String secret = "password_for_connection";
    String vhost = "virtual_host_to_connect";
    ArrayList<SBChannel> channels = new ArrayList<>();
    
    SBDevice device = new SBDevice.Builder()
                        .setDeviceId(device_id)
                        .setDeviceName(device_name)
                        .setHost(host)
                        .setSecret(secret)
                        .setVHost(vhost)
                        .setChannels(channels)
                        .getDevice();

    SBDevice custom_device = new SBDevice(<device_custom_configuration>);
    final SpaceBunny.Client spaceBunny = new SpaceBunny.Client(device);
} catch (SpaceBunny.ConfigurationException ex) {
    ex.printStackTrace();
}
```

Add a FinishConfigiurationListener to reach all device information:

```java
[...]
final SpaceBunny.Client spaceBunny = new SpaceBunny.Client(device_key);
spaceBunny.setOnFinishConfigiurationListener(new SpaceBunny.OnFinishConfigiurationListener() {
                @Override
                public void onConfigured(SBDevice device) throws SpaceBunny.ConnectionException {
                    System.out.println(device.toString());
                }
            });
[...]
```

After configuration, set up the client if needed:

```java
[...]
// Turn off secure connection or certificate verification
spaceBunny.setTls(false);

spaceBunny.setVerifyCA(false);

// Set a custom certificate
spaceBunny.setPathCustomCA("<absolute_path>\\cert.pem");
[...]
```

#### Connection

After you have configurated your Space Bunny client connect with simple methods.
Connection use default protocol [AMQP](https://www.amqp.org/), to use a different one please contact us.

```java
[...]
spaceBunny.connect();

// Connection with custom callback
spaceBunny.connect(new SpaceBunny.OnConnectedListener() {
                @Override
                public void onConnected() throws SpaceBunny.ConnectionException {
        
    }
});
[...]
```

Close connection when you have done:
```java
[...]
try {
    spaceBunny.close();
} catch (SpaceBunnyConfigurationException ex) {
    ex.printStackTrace();
}
```

#### AMQP publisher

In this example a device publishes a single message on one `channel`:

```java
[...]
spaceBunny.publish(channel_name, msg, headers, callBack);
[...]
```

*channel_name* [String] is the name of the selected channel (It would be send a warning if channel does not exist.);
*msg* [String] is the message to send to channel
*headers* [@Nullable HashMap<String, Object>] are the headers to send to the channel
*callBack* [@Nullable com.rabbitmq.client.ConfirmListener] callBack to publish Ack and Nack

It can be useful to get all avaiable channel of the device with:

```java
[...]
ArrayList<SBChannel> channels = spaceBunny.getChannels();
[...]
```

#### AMQP subscribe

In this example a device waits for incoming messages on its `inbox` channel.
You have to configure and connect your client, then you can add a callBack function on subscribe to inbox channel.

```java
[...]
spaceBunny.subscribe(new RabbitConnection.OnSubscriptionMessageReceivedListener() {
                        @Override
                        public void onReceived(String message, Envelope envelope) {
                            System.out.println(message);
                        }
                    });
[...]
```

## License

The library is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).