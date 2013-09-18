storm-rabbitmq-json-test
========================

**storm-rabbitmq-json-test:** This toplogy also consumes tuples from strom-amqp-spout, however, the client generates petitions using JSON class
  
  *dependecies:*
  
  * https://github.com/davidpr/storm-amqp-spout
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-json
  * https://github.com/davidpr/storm-json

### Usage

1. Compile and install project dependencies form each project dependency root directory

    a) [Storm-amqp-spout](https://github.com/davidpr/storm-amqp-spout)
    
    b) [Storm-json](https://github.com/davidpr/storm-json)

2. Compile the project from the project root directory

     `mvn package`
    
3. [Initialize](https://github.com/davidpr/storm-tutorial/wiki/Single-node-installation#initializing-storm) Storm

4. Run the topology with Storm 

    `./storm jar /home/user/storm-rabbitmq/storm-rabbitmq-json-test/target/storm-rabbitmq-json-test-1.0-SNAPSHOT.jar`
    `com.davidpr.rabbitmq.Rabbitmqopology topologyname`
    
5. [Compile and use](https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-json) the client




  


