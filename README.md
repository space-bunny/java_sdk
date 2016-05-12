<p align="center">
  <img width="480" src="assets/logo.png"/>
</p>

[![NPM](https://img.shields.io/npm/v/spacebunny.svg?style=flat-square)](https://www.npmjs.com/package/spacebunny)

[Space Bunny](http://spacebunny.io) is the IoT platform that makes it easy for you and your devices to send and exchange messages with a server or even with each other. You can store the data, receive timely event notifications, monitor live streams and remotely control your devices. Easy to use, and ready to scale at any time.

This is the source code repository for Java SDK.
Please feel free to contribute!

## Installation



## Basic usage

### Device

Devices can publish messages on configured channels and receive messages on their `inbox` channel

#### AMQP publisher

In this example a device publishes a single message on the first configured channel

```java
String device_key = "device_identifer";

try {
    final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);

    spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
        @Override
        public void onConnected() throws SpaceBunnyConnectionException {
            ArrayList<Channel> channels = spaceBunny.getChannels();

            spaceBunny.publish(channels.get(0), "Something to share");
            
        }
    });

} catch (SpaceBunnyConnectionException ex) {
    ex.printStackTrace();
}
```

#### AMQP receiver

In this example a device waits for incoming messages on its `inbox` channel

```javascript
String device_key = "device_identifer";

try {
    final SpaceBunnyClient spaceBunny = new SpaceBunnyClient(device_key);

    spaceBunny.connect(new SpaceBunnyClient.OnConnectedListener() {
        @Override
        public void onConnected() throws SpaceBunnyConnectionException {
            ArrayList<Channel> channels = spaceBunny.getChannels();
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

For more advanced usage please refer to example files in `examples` folder

### Device

#### STOMP receiver and publisher

In this example a device waits for incoming messages on its `inbox` channel and publishes a single message on the first configured channel

```html
<script src="https://raw.githubusercontent.com/space-bunny/node-sdk/master/dist/spacebunny.js"></script>
<script>
  [...]
  // Use your Api Key
  var connectionParams = { apiKey: 'your-api-key' };
  var webStompClient = new StompClient(connectionParams);
  webStompClient.onReceive(messageCallback).then(function(res) {
    console.log('Successfully connected!');
  }).catch(function(reason) {
    console.error(reason);
  });
  var content = { some: 'json' };
  var channels = webStompClient.channels();
  webStompClient.publish(channels[0], content).then(function(res) {
    console.log('Message published!');
  }).catch(function(reason) {
    console.error(reason);
  });
  [...]
</script>
```

## License

The library is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).