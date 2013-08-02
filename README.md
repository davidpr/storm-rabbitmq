storm-rabbitmq
==============

Set of projects containing spouts, bolts, clients, etc. to use Storm with Rabbitmq and JSON

List of Storm Topologies and Bolts:

storm-rabbitmq-test -> This toplogy consumes tuples from storm-amqp-spout. To generate petitions a java client is used.

  dependencies:
  https://github.com/davidpr/storm-amqp-spout
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-rabbitmq-client-test

storm-rabbitmq-json-test -> This toplogy also consumes tuples from strom-amqp-spout, however, the client generates petitions using JSON
  
  dependecies:
  https://github.com/davidpr/storm-amqp-spout
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-json
  https://github.com/davidpr/storm-json
  
  
storm-rabbitmq-rpc-json-test -> This topology consumes tuples from storm-amqp-rpc-spout
  
  dependencies:
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-amqp-spout-rpc
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-rpc-json
  https://github.com/davidpr/storm-json
  
storm-rabbitmq-kyo-rpc-json-test

  dependencies:
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-amqp-spout-kryo-rpc-json
  https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-rpc-json
  https://github.com/davidpr/storm-json

List of Spouts

storm-amqp-spout-rpc ->
storm-amqp-spout-kryo-rpc-json ->


List of Clients

storm-rabbitmq-client-test ->
storm-client-json ->
storm-client-rpc-json ->



