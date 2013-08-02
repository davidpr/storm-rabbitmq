storm-rabbitmq
==============

Set of projects containing spouts, bolts, clients, etc. to use Storm with Rabbitmq and JSON

### List of Storm Topologies and Bolts

---

**storm-rabbitmq-test:** This toplogy consumes tuples from storm-amqp-spout. To generate petitions a java client is used.

  *dependencies:*
  * https://github.com/davidpr/storm-amqp-spout
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-rabbitmq-client-test
  
---

**storm-rabbitmq-json-test:** This toplogy also consumes tuples from strom-amqp-spout, however, the client generates petitions using JSON class
  
  *dependecies:*
  
  * https://github.com/davidpr/storm-amqp-spout
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-json
  * https://github.com/davidpr/storm-json
  
---  

**storm-rabbitmq-rpc-json-test:** This topology consumes tuples from storm-amqp-spout-rpc, additionaly, the spout uses Rabbitmq RPC functionality.
Here the spout creates a response queue in which it delivers responses via RPC.
  
  *dependencies:*
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-amqp-spout-rpc
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-rpc-json
  * https://github.com/davidpr/storm-json
  
---  

**storm-rabbitmq-kyo-rpc-json-test:** This topology consumes tuples from storm-amqp-spout-kryo-rpc-json.
The point is that this spout is esentially the same as the storm-amqp-spout becuase is this topology it is the bolt
the responsible for answering via RPC insted of the bolt. However, this spout needs to pass down to the bolt 
some extra information, so responses are matched with requests.

  *dependencies:*
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-amqp-spout-kryo-rpc-json
  * https://github.com/davidpr/storm-rabbitmq/tree/master/storm-client-rpc-json
  * https://github.com/davidpr/storm-json

---

### List of Spouts

---

**storm-amqp-spout-rpc:**

---

**storm-amqp-spout-kryo-rpc-json:**


### List of Clients

---

**storm-rabbitmq-client-test:**

---

**storm-client-json:**

---

**storm-client-rpc-json:**



