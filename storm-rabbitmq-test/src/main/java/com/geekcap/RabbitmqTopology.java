package com.geekcap.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import backtype.storm.StormSubmitter;


import backtype.storm.task.ShellBolt;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;

//importing the Scheme class
import backtype.storm.spout.Scheme;

/////rabbitmq, amqp and amqpspout includes
import com.rapportive.storm.spout.AMQPSpout;
import com.rapportive.storm.amqp.QueueDeclaration;
import com.rapportive.storm.amqp.SharedQueueWithBinding;
import com.rapportive.storm.amqp.ExclusiveQueueWithBinding;
//import com.rapportive.storm.amqp.HAPolicy;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.*;
////////////////////////////////////////////////

public class RabbitmqTopology 
{
    public static void main(String[] args) throws Exception 
    {
        TopologyBuilder builder = new TopologyBuilder();

	QueueDeclaration qd = new SharedQueueWithBinding("stormqueue", "stormexchange", "stormkey");
	//rabbitmq needed next calls are in SetupAMQP method of AMQPSpout class; calls are below just as a reminder
	//ConnectionFactory factory = new ConnectionFactory();
	//factory.setHost("localhost");
	//Connection connection = factory.newConnection();
	//Channel channel = connection.createChannel();
	Scheme scheme = new BasicScheme();

        builder.setSpout( "spout", new AMQPSpout(   "127.0.0.1", 5672, "guest", "guest", "/", qd, scheme));

        builder.setBolt( "rabbitbolt", new RabbitmqBolt() )
                .shuffleGrouping("spout");

        //Config conf = new Config();
        //LocalCluster cluster = new LocalCluster();
        //cluster.submitTopology("test", conf, builder.createTopology());
        //Utils.sleep(10000);
        //cluster.killTopology("test");
        //cluster.shutdown();
	Config conf = new Config();
	conf.setDebug(true);
	conf.setNumWorkers(1);
	conf.setMaxSpoutPending(5000);

	try{
		StormSubmitter.submitTopology( args[0], conf, builder.createTopology() );
	}
	catch(AlreadyAliveException e){
		
	}
    }
}
