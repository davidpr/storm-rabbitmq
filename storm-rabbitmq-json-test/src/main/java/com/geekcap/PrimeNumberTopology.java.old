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

public class PrimeNumberTopology 
{
    public static void main(String[] args) throws Exception 
    {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout( "spout", new PrimeNumberSpout() );
        builder.setBolt( "prime", new PrimeNumberBolt() )
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
