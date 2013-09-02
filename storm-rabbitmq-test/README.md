storm-rabbitmq-test
===================

This toplogy consumes tuples from storm-amqp-spout. To generate petitions a java client is used.

  *dependencies:*
  * https://github.com/davidpr/storm-amqp-spout
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-rabbitmq-client-test

### Usage

1. [Compile and install](https://github.com/davidpr/storm-amqp-spout) project dependencies form each project dependency root directory

2. Compile the project from the project root directory

     `mvn package`
    
3. [Initialize](https://github.com/davidpr/storm-tutorial/wiki/Single-node-installation#initializing-storm) Storm

4. Run the topology with Storm 

    `./storm jar /home/user/storm-rabbitmq/storm-rabbitmq-test/target/storm-rabbitmq-test-1.0-SNAPSHOT.jar`
    `com.davidpr.rabbitmq.Rabbitmqopology topologyname`
    
5. [Compile and use]() the client




  


