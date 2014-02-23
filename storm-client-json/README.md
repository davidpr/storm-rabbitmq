storm-client-json
=================

This client sends a request to a AMQP spout using JSON class.

### Usage

1. Compile the client.

    ``

2. Have a [receiver listening] (https://github.com/davidpr/storm-rabbitmq/tree/master/storm-rabbitmq-json-testt) to this client petitions.

3. Run the client.

    `java -cp .:/home/user/Downloads/rabbitmq-java-client-bin-3.1.1/commons-io-1.2.jar:/home/user/Downloads/`
    `rabbitmq-java-client-bin-3.1.1/commons-cli-1.1.jar:/home/user/Downloads/rabbitmq-java-client-bin-3.1.1/`
    `rabbitmq-client.jar StormSender stormkey message`

As it can be seen, some .jar have to be included in the classpath. Stormkey is the routing key so the exchange knows where to route the message. Message is the message as a string.
