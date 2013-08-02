package com.geekcap.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import java.io.*;
//rabbitmq part
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.rabbitmq.client.AMQP.BasicProperties;

import org.apache.log4j.Logger;
import java.io.IOException;

public class RabbitmqKRYORPCJSONBolt extends BaseRichBolt 
{
    private static final Logger log = Logger.getLogger(PrimeNumberBolt.class);

    private OutputCollector collector;

	private final String boltHost;
	private final int boltPort;
     	private final String boltUsername;
     	private final String boltPassword;
     	private final String boltVhost;
     	//private final boolean requeueOnFail;

    //rabbitmq part
    private transient Connection boltConnection;
    private transient Channel boltChannel;

    public PrimeNumberBolt(String host, int port, String username, String password, String vhost){//, QueueDeclaration queueDeclaration, Scheme scheme ){
		
	 log.info("hi Im spout creation 2 info");
         this.boltHost = host;
         this.boltPort = port;
         this.boltUsername = username;
         this.boltPassword = password;
         this.boltVhost = vhost;
         //this.queueDeclaration = queueDeclaration;//you don't need this
         //this.requeueOnFail = requeueOnFail;
         //this.serialisationScheme = scheme;//you may don't need this

    }

    public void prepare( Map conf, TopologyContext context, OutputCollector collector ) 
    {
        this.collector = collector;

	//rabbitmq part
	final ConnectionFactory connectionFactory = new ConnectionFactory();

	try{
		this.boltConnection = connectionFactory.newConnection();
        	this.boltChannel = boltConnection.createChannel();
	}
	catch(IOException e){
		log.error("Error when publishing to create ConnectionFactory in PrimeNumberBolt",e);	
	
	}

        connectionFactory.setHost(boltHost);
        connectionFactory.setPort(boltPort);
        connectionFactory.setUsername(boltUsername);
        connectionFactory.setPassword(boltPassword);
        connectionFactory.setVirtualHost(boltVhost);
    }

    public void execute( Tuple tuple )
    {
        //int number = tuple.getInteger( 0 );//old

	Fields fields = tuple.getFields();
    	int numFields = fields.size();
 	System.out.println("numer of fields: " + numFields + " \n");

    	for (int idx = 0; idx < numFields; idx++) {
        	String name = fields.get(idx);
        	Object value = tuple.getValue(idx);
		System.out.println("hi im bolt loop\n");
		System.out.println("Field name: " + name +", Field value: " + value +" \n"); 
    	}
        //if( isPrime( number) )
        //{
        //    System.out.println( number );
        //}
	//////////only for rpc when bolt wants to respond (in my case storm_rabbitmq-kryo-rpc-json-test///
	//BasicProperties props = delivery.getProperties();
        /*BasicProperties replyProps = new BasicProperties
                                          .Builder()
                                          .correlationId(props.getCorrelationId())
                                          .build();*/
	////
	try{
        	String response="40";
		
		BasicProperties props = (BasicProperties)tuple.getValue(1);
		BasicProperties replyProps = (BasicProperties)tuple.getValue(2);	
              //this.amqpChannel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8    "));
       		this.boltChannel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                //this.amqpChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		long dtag=(Long)tuple.getValue(3);	
                this.boltChannel.basicAck(dtag, false);
        }
	catch(IOException e){
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	

	System.out.println("!!!!! hi Im bolt!!!!!!1\n");
        collector.ack( tuple );
    }

    public void declareOutputFields( OutputFieldsDeclarer declarer ) 
    {
        //declarer.declare( new Fields( "number" ) );
        declarer.declare( new Fields( "deliveryTag", "bytes", "otherbytes", "dt"));
    }   
    
    private boolean isPrime( int n ) 
    {
        if( n == 1 || n == 2 || n == 3 )
        {
            return true;
        }
        
        // Is n an even number?
        if( n % 2 == 0 )
        {
            return false;
        }
        
        //if not, then just check the odds
        for( int i=3; i*i<=n; i+=2 ) 
        {
            if( n % i == 0)
            {
                return false;
            }
        }
        return true;
    }
}
