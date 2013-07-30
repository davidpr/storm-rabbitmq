package com.geekcap.storm;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

public class BasicScheme implements Scheme {
 
    public List<Object> deserialize(byte[] ser) {//parameters not changed
    // TODO Auto-generated method stub
        ArrayList li = new ArrayList();
        String decoded = "";
        try {
            decoded = new String(ser, "UTF-8");
            decoded = decoded.replaceAll("\n", "");
		
		li.add(decoded);
                
		//String decoded2 = "aabbcc";
                //li.add(decoded2);
		//li.add(String.valueOf(deliveryTag));

        } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return li;
	/*try {
            return new Values(new String(ser, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }*/
	
    }
    public Fields getOutputFields() {
        Fields fields = new Fields("deliveryTag", "bytes", "otherbytes");
        // TODO Auto-generated method stub
        return fields;
    }
}
