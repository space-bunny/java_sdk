<p align="center">
  <img width="480" src="assets/logo.png"/>
</p>

[Space Bunny](http://spacebunny.io) is the IoT platform that makes it easy for you and your devices to send and exchange messages with a server or even with each other. You can store the data, receive timely event notifications, monitor live streams and remotely control your devices. Easy to use, and ready to scale at any time.

This is the source code repository for Java SDK.
Please feel free to contribute!

## Installation

Gradle: 

```
compile 'io.spacebunny:device-sdk:0.5'
```

Maven: 

```
<dependency>
    <groupId>io.spacebunny</groupId>
    <artifactId>device-sdk</artifactId>
    <version>0.5</version>
</dependency>
```

## Basic usage

### Device

Devices can publish messages on configured channels and receive messages on their `inbox` channel

#### Configuration

Configure the instance of the SpaceBunny's Client with a valid Device Key:

```java
try {
    final SpaceBunnyClient spaceBunny = new SpaceBunnyClient("device_key");
    SpaceBunnyClient.OnFinishConfigiurationListener() {
        @Override
        public void onConfigured(SBDevice device) throws SpaceBunnyConnectionException {
            System.out.println(device.toString());
        }
    });
} catch (SpaceBunnyConfigurationException ex) {
    ex.printStackTrace();
}
```

Set up the client if needed:

```java
// Turn off secure connection or certificate verification
spaceBunny.setSsl(false);

spaceBunny.setVerifyCA(false);

// Set a custom certificate
spaceBunny.setPathCustomCA("<absolute_path>\\cert.pem");
```

#### Connection

Connect to SpaceBunny with multiple parameters 

```java
// Connection with default protocol (AMQP)
spaceBunny.connect();

// Connection with custom callback
spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
    @Override
    public void onConnected() throws SpaceBunnyConnectionException {
        
    }
});

// Connection with custom protocol and custom callback
spaceBunny.connect(new SBProtocol(), new SpaceBunnyClient.OnConnectedListener() {
    @Override
    public void onConnected() throws SpaceBunnyConnectionException {
        
    }
});
```

Close connection when you have done:
```java
try {
    spaceBunny.close();
} catch (SpaceBunnyConfigurationException ex) {
    ex.printStackTrace();
}
```

#### SpaceBunnyClient Read Attributes

```java
spaceBunny.getProtocols();

spaceBunny.getChannels();
```

#### AMQP publisher

In this example a device publishes a single message on the first configured channel

```java
String device_key = "device_identifer";

try {
    final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);

    spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
        @Override
        public void onConnected() throws SpaceBunnyConnectionException {
            ArrayList<SBChannel> channels = spaceBunny.getChannels();

            spaceBunny.publish(channels.get(0), "Something to share");
            
        }
    });

} catch (SpaceBunnyConnectionException ex) {
    ex.printStackTrace();
}
```

#### AMQP subscribe

In this example a device waits for incoming messages on its `inbox` channel

```java
String device_key = "device_identifer";

try {
    final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);

    spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
        @Override
        public void onConnected() throws SpaceBunnyConnectionException {
            ArrayList<SBChannel> channels = spaceBunny.getChannels();
            spaceBunny.subscribe(new RabbitConnection.OnSubscriptionMessageReceivedListener() {
                @Override
                public void onReceived(String message, Envelope envelope) {
                    System.out.println(message);
                }
            });
        }
    });

} catch (SpaceBunnyConnectionException ex) {
    ex.printStackTrace();
}
```

## Some useful concepts

#### Device

Build an instance of a device, configure and use it:

```java
String host = "host_to_connect";
String device_name = "name_of_the_device";
String device_id = "device_identifer";
String secret = "password_for_connection";
String vhost = "virtual_host_to_connect";
ArrayList<SBProtocol> protocols = new ArrayList<>(Costants.min_protocols); // At least one protocol
ArrayList<SBChannel> channels = new ArrayList<>(); // At least one channel

SBDevice device = new SBDevice.Builder()
                    .setDeviceId(device_id)
                    .setDeviceName(device_name)
                    .setHost(host)
                    .setSecret(secret)
                    .setVHost(vhost)
                    .setChannels(channels)
                    .setProtocols(protocols)
                    .getDevice();
```

#### Protocol

Get all device protocols and use it:

```java
ArrayList<SBProtocol> protocols = spaceBunny.getProtocols();
```

Or create one:

```java
String name = "name_of_the_protocol";
int port = port_to_connect;
int ssl_port = ssl_port_for_secure_connection;

SBProtocol protocol = new SBProtocol(name, port, ssl_port);
```

#### Channel

Get all device channels and use it:

```java
ArrayList<SBChannel> channels = spaceBunny.getChannels();
```

Or create one:
```java
String id = "id_of_the_channel";
String name = "name_of_the_channel";

SBChannel channel = new SBChannel(id, name);
```

## License

The library is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).