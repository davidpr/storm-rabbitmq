package com.rapportive.storm.spout;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.Queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

//AMQPSpout RPC
import com.rabbitmq.client.AMQP.BasicProperties;

//
import com.rapportive.storm.amqp.QueueDeclaration;
import backtype.storm.spout.Scheme;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;

import backtype.storm.utils.Utils;

import java.util.ArrayList;
//import java.util.List;
//import java.lang.String;

/**
 * Spout to feed messages into Storm from an AMQP queue.  Each message routed
 * to the queue will be emitted as a Storm tuple.  The message will be acked or
 * rejected once the topology has respectively fully processed or failed the
 * corresponding tuple.
 *
 * <p><strong>N.B.</strong> if you need to guarantee all messages are reliably
 * processed, you should have AMQPSpout consume from a queue that is
 * <em>not</em> set as 'exclusive' or 'auto-delete': otherwise if the spout
 * task crashes or is restarted, the queue will be deleted and any messages in
 * it lost, as will any messages published while the task remains down.  See
 * {@link com.rapportive.storm.amqp.SharedQueueWithBinding} to declare a shared
 * queue that allows for guaranteed processing.  (For prototyping, an
 * {@link com.rapportive.storm.amqp.ExclusiveQueueWithBinding} may be
 * simpler to manage.)</p>
 *
 * <p><strong>N.B.</strong> this does not currently handle malformed messages
 * (which cannot be deserialised by the provided {@link Scheme}) very well:
 * the spout worker will crash if it fails to serialise a message.</p>
 *
 * <p>This consumes messages from AMQP asynchronously, so it may receive
 * messages before Storm requests them as tuples; therefore it buffers messages
 * in an internal queue.  To avoid this buffer growing large and consuming too
 * much RAM, set {@link #CONFIG_PREFETCH_COUNT}.</p>
 *
 * <p>This spout can be distributed among multiple workers, depending on the
 * queue declaration: see {@link QueueDeclaration#isParallelConsumable}.</p>
 *
 * @see QueueDeclaration
 * @see com.rapportive.storm.amqp.SharedQueueWithBinding
 * @see com.rapportive.storm.amqp.ExclusiveQueueWithBinding
 *
 * @author Sam Stokes (sam@rapportive.com)
 */
public class AMQPSpout implements IRichSpout {
    private static final long serialVersionUID = 11258942292629264L;

    private static final Logger log = Logger.getLogger(AMQPSpout.class);

    /**
     * Storm config key to set the AMQP basic.qos prefetch-count parameter.
     * Defaults to 100.
     *
     * <p>This caps the number of messages outstanding (i.e. unacked) at a time
     * that will be sent to each spout worker.  Increasing this will improve
     * throughput if the network roundtrip time to the AMQP broker is
     * significant compared to the time for the topology to process each
     * message; this will also increase the RAM requirements as the internal
     * message buffer grows.</p>
     *
     * <p>AMQP allows a prefetch-count of zero, indicating unlimited delivery,
     * but that is not allowed here to avoid unbounded buffer growth.</p>
     */
    public static final String CONFIG_PREFETCH_COUNT = "amqp.prefetch.count";
    private static final long DEFAULT_PREFETCH_COUNT = 100;

    /**
     * Time in milliseconds to wait for a message from the queue if there is
     * no message ready when the topology requests a tuple (via
     * {@link #nextTuple()}).
     */
    public static final long WAIT_FOR_NEXT_MESSAGE = 1L;

    /**
     * Time in milliseconds to wait after losing connection to the AMQP broker
     * before attempting to reconnect.
     */
    public static final long WAIT_AFTER_SHUTDOWN_SIGNAL = 10000L;

    /**
     * Name of the stream where malformed deserialized messages are sent for
     * special handling. Generally with a {@link Scheme} implementation returns
     * null or a zero-length tuple
     */
    public static String ERROR_STREAM_NAME = "error-stream";

    private final String amqpHost;
    private final int amqpPort;
    private final String amqpUsername;
    private final String amqpPassword;
    private final String amqpVhost;
    private final boolean requeueOnFail;

    private final QueueDeclaration queueDeclaration;

    private final Scheme serialisationScheme;

    private transient boolean spoutActive = true;
    private transient Connection amqpConnection;
    private transient Channel amqpChannel;
    private transient QueueingConsumer amqpConsumer;
    private transient String amqpConsumerTag;

    private SpoutOutputCollector collector;

    private int prefetchCount;

    /**
     * Create a new AMQP spout.  When
     * {@link #open(Map, TopologyContext, SpoutOutputCollector)} is called, it
     * will declare a queue according to the specified
     * <tt>queueDeclaration</tt>, subscribe to the queue, and start consuming
     * messages.  It will use the provided <tt>scheme</tt> to deserialise each
     * AMQP message into a Storm tuple. Note that failed messages will not be 
     * requeued.
     *
     * @param host  hostname of the AMQP broker node
     * @param port  port number of the AMQP broker node
     * @param username  username to log into to the broker
     * @param password  password to authenticate to the broker
     * @param vhost  vhost on the broker
     * @param queueDeclaration  declaration of the queue / exchange bindings
     * @param scheme  {@link backtype.storm.spout.Scheme} used to deserialise
     *          each AMQP message into a Storm tuple
     */
    public AMQPSpout(String host, int port, String username, String password, String vhost, QueueDeclaration queueDeclaration, Scheme scheme) {

         this(host, port, username, password, vhost, queueDeclaration, scheme, false);
	 //log.info("hi Im spout creation info");
         //log.warn("hi IM spout creation warm");
    }

    /**
     * Create a new AMQP spout.  When
     * {@link #open(Map, TopologyContext, SpoutOutputCollector)} is called, it
     * will declare a queue according to the specified
     * <tt>queueDeclaration</tt>, subscribe to the queue, and start consuming
     * messages.  It will use the provided <tt>scheme</tt> to deserialise each
     * AMQP message into a Storm tuple.
     *
     * @param host  hostname of the AMQP broker node
     * @param port  port number of the AMQP broker node
     * @param username  username to log into to the broker
     * @param password  password to authenticate to the broker
     * @param vhost  vhost on the broker
     * @param queueDeclaration  declaration of the queue / exchange bindings
     * @param scheme  {@link backtype.storm.spout.Scheme} used to deserialise
     *          each AMQP message into a Storm tuple
     * @param requeueOnFail  whether messages should be requeued on failure 
     */
    public AMQPSpout(String host, int port, String username, String password, String vhost, QueueDeclaration queueDeclaration, Scheme scheme, boolean requeueOnFail) {

        this.amqpHost = host;
        this.amqpPort = port;
        this.amqpUsername = username;
        this.amqpPassword = password;
        this.amqpVhost = vhost;
        this.queueDeclaration = queueDeclaration;
        this.requeueOnFail = requeueOnFail;
        
        this.serialisationScheme = scheme;
    }


    /**
     * Acks the message with the AMQP broker.
     */
    @Override
    public void ack(Object msgId) {
        if (msgId instanceof Long) {
            final long deliveryTag = (Long) msgId;
            if (amqpChannel != null) {
                try {
                    amqpChannel.basicAck(deliveryTag, false /* not multiple */);
                } catch (IOException e) {
                    log.warn("Failed to ack delivery-tag " + deliveryTag, e);
                } catch (ShutdownSignalException e) {
                    log.warn("AMQP connection failed. Failed to ack delivery-tag " + deliveryTag, e);
                }
            }
        } else {
            log.warn(String.format("don't know how to ack(%s: %s)", msgId.getClass().getName(), msgId));
        }
    }


    /**
     * Cancels the queue subscription, and disconnects from the AMQP broker.
     */
    @Override
    public void close() {
        try {
            if (amqpChannel != null) {
              if (amqpConsumerTag != null) {
                  amqpChannel.basicCancel(amqpConsumerTag);
              }

              amqpChannel.close();
            }
        } catch (IOException e) {
            log.warn("Error closing AMQP channel", e);
        }

        try {
            if (amqpConnection != null) {
              amqpConnection.close();
            }
        } catch (IOException e) {
            log.warn("Error closing AMQP connection", e);
        }
    }

    /**
     * Resumes a paused spout
     */
    public void activate() {
        log.info("Unpausing spout");
        spoutActive = true;
    }

    /**
     * Pauses the spout
     */
    public void deactivate() {
        log.info("Pausing spout");
        spoutActive = false;
    }


    /**
     * Tells the AMQP broker to drop (Basic.Reject) the message.
     *
     * requeueOnFail constructor parameter determines whether the message will be requeued.
     * 
     * <p><strong>N.B.</strong> There's a potential for infinite
     * redelivery in the event of non-transient failures (e.g. malformed
     * messages). 
     *
     */
    @Override
    public void fail(Object msgId) {
        if (msgId instanceof Long) {
            final long deliveryTag = (Long) msgId;
            if (amqpChannel != null) {
                try {
                    amqpChannel.basicReject(deliveryTag, requeueOnFail);
                } catch (IOException e) {
                    log.warn("Failed to reject delivery-tag " + deliveryTag, e);
                }
            }
        } else {
            log.warn(String.format("don't know how to reject(%s: %s)", msgId.getClass().getName(), msgId));
        }
    }
	/*public void publishjson( BasicProperties props, BasicProperties replyProps)    {
                 String response="40";
		try{
                this.amqpChannel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8    "));    			}
		catch (IOException e){
			log.warn("hihi");
		}
                // this.amqpChannel.basicAck(delivery, false);
 
         }*/


    /**
     * Emits the next message from the queue as a tuple.
     *
     * Serialization schemes returning null will immediately ack
     * and then emit unanchored on the {@link #ERROR_STREAM_NAME} stream for
     * further handling by the consumer.
     *
     * <p>If no message is ready to emit, this will wait a short time
     * ({@link #WAIT_FOR_NEXT_MESSAGE}) for one to arrive on the queue,
     * to avoid a tight loop in the spout worker.</p>
     */
    @Override
    public void nextTuple() {
        if (spoutActive && amqpConsumer != null) {
            try {
                final QueueingConsumer.Delivery delivery = amqpConsumer.nextDelivery(WAIT_FOR_NEXT_MESSAGE);
		
                if (delivery == null) return;
                final long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                final byte[] message = delivery.getBody();
		
		///////////////////new rpc
		BasicProperties props = delivery.getProperties();
        	BasicProperties replyProps = new BasicProperties
                                         .Builder()
                                         .correlationId(props.getCorrelationId())
                                         .build();
		//////////////////////////////
                List<Object> deserializedMessage = serialisationScheme.deserialize(message);
                if (deserializedMessage != null && deserializedMessage.size() > 0) {
		//let's see what's inside the Object List (checking)
		/*System.out.println("Lenght of the list : "+ deserializedMessage.size() +"\n");
		for (int i =0; i< deserializedMessage.size(); i++){
			Object obj=deserializedMessage.get(i);
			System.out.println("Object value: "+  obj + "\n" );
		}*/
			ArrayList li = new ArrayList();
			li.add(deserializedMessage.get(0));			
			li.add(props);
			li.add(replyProps);
			deserializedMessage=li;
                    	collector.emit(deserializedMessage, deliveryTag);
			///////////new for AMQPSpout RPC+JSON
			try{
			String response="your name was: "+deserializedMessage.get(0)+" " ;
			this.amqpChannel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
          		//this.amqpChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          		this.amqpChannel.basicAck(deliveryTag, false);
			}
			///publishjson(props, replyProps, delivery.detEnvelope().getDeliveryTag());
			//try{
			//	publishjson(props, replyProps);
			//}
			catch(IOException e){
			 log.error("Error when publishing to the response queue",e);
			}
			////////////////
                } else {
                    handleMalformedDelivery(deliveryTag, message);
                }
            } catch (ShutdownSignalException e) {
                log.warn("AMQP connection dropped, will attempt to reconnect...");
                Utils.sleep(WAIT_AFTER_SHUTDOWN_SIGNAL);
                reconnect();
            } catch (InterruptedException e) {
                // interrupted while waiting for message, big deal
            }
        }
    }

	/*public void publishjson( BasicProperties props, BasicProperties replyProps, int delivery ) throws Exception{

		String response="40";
		this.amqpChannel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8    "));		          
		this.amqpChannel.basicAck(delivery, false);

	}*/
    /**
     * Connects to the AMQP broker, declares the queue and subscribes to
     * incoming messages.
     */
    @Override
    public void open(@SuppressWarnings("rawtypes") Map config, TopologyContext context, SpoutOutputCollector collector) {
        Long prefetchCount = (Long) config.get(CONFIG_PREFETCH_COUNT);
        if (prefetchCount == null) {
            log.info("Using default prefetch-count");
            prefetchCount = DEFAULT_PREFETCH_COUNT;
        } else if (prefetchCount < 1) {
            throw new IllegalArgumentException(CONFIG_PREFETCH_COUNT + " must be at least 1");
        }
        this.prefetchCount = prefetchCount.intValue();

        try {
            this.collector = collector;

            setupAMQP();
        } catch (IOException e) {
            log.error("AMQP setup failed", e);
        }
    }


    /**
     * Acks the bad message to avoid retry loops. Also emits the bad message
     * unreliably on the {@link #ERROR_STREAM_NAME} stream for consumer handling.
     * @param deliveryTag AMQP delivery tag
     * @param message bytes of the bad message
     */
    private void handleMalformedDelivery(long deliveryTag, byte[] message) {
        log.debug("Malformed deserialized message, null or zero-length. " + deliveryTag);
        ack(deliveryTag);
        collector.emit(ERROR_STREAM_NAME, new Values(deliveryTag, message));
    }

    private void setupAMQP() throws IOException {
        final int prefetchCount = this.prefetchCount;

        final ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost(amqpHost);
        connectionFactory.setPort(amqpPort);
        connectionFactory.setUsername(amqpUsername);
        connectionFactory.setPassword(amqpPassword);
        connectionFactory.setVirtualHost(amqpVhost);

        this.amqpConnection = connectionFactory.newConnection();
        this.amqpChannel = amqpConnection.createChannel();

        log.info("Setting basic.qos prefetch-count to " + prefetchCount);
        amqpChannel.basicQos(prefetchCount);

        final Queue.DeclareOk queue = queueDeclaration.declare(amqpChannel); //original line
	////String queueName = amqpChannel.queueDeclare().getQueue();from example 4
	//amqpChannel.queueDeclare("stormqueue", false, false, false, null);//from example 6
	//channel.queueBind("stormqueue", "stormexchange", "error");//from example 6

        final String queueName = queue.getQueue(); //original line
	//final String queueName = "stormqueue"; //added line davidp

        //log.info("Consuming queue AMQPSpout RPC+JSON!!! " + queueName);

        this.amqpConsumer = new QueueingConsumer(amqpChannel);
        this.amqpConsumerTag = amqpChannel.basicConsume(queueName, false /* no auto-ack */, amqpConsumer);
    }


    private void reconnect() {
        log.info("Reconnecting to AMQP broker...");
        try {
            setupAMQP();
        } catch (IOException e) {
            log.warn("Failed to reconnect to AMQP broker", e);
        }
    }


    /**
     * Declares the output fields of this spout according to the provided
     * {@link backtype.storm.spout.Scheme}.
     *
     * Additionally declares an error stream (see {@link #ERROR_STREAM_NAME} for handling
     * malformed or empty messages to avoid infinite retry loops
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(serialisationScheme.getOutputFields());
        declarer.declareStream(ERROR_STREAM_NAME, new Fields("deliveryTag",     "bytes", "otherbytes"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
