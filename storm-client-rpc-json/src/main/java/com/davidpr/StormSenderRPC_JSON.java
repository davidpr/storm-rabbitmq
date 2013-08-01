import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;


import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.UUID;

//to create json objects
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.io.*; //needs to be included for the stringwriter

public class StormSenderRPC_JSON {

  private static final String EXCHANGE_NAME = "stormexchange";

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    //to reply
    String replyQueueName;
    QueueingConsumer consumer;
    
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    String severity = getSeverity(argv);
    String message = getMessage(argv);

    replyQueueName = channel.queueDeclare().getQueue(); 
    consumer = new QueueingConsumer(channel);
    channel.basicConsume(replyQueueName, true, consumer);
    
	//put the string into a JSON text
	JSONObject obj = new JSONObject();
	obj.put("name",message);
	StringWriter out = new StringWriter();
	JSONValue.writeJSONString(obj,out);
	String jsonText = out.toString();
	//System.out.println("I'm client and the text of my json object is: "+ jsonText +"\n");

	String corrId = UUID.randomUUID().toString();
	BasicProperties props = new BasicProperties
                                .Builder()
                                .correlationId(corrId)
                                .replyTo(replyQueueName)
                                .build();
	
    channel.basicPublish(EXCHANGE_NAME, severity, props, jsonText.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + severity + "':'" + jsonText + "'");

    String response = null;
    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response = new String(delivery.getBody(),"UTF-8");
        break;
      }
     }
     System.out.println(" [.] Got '" + response + "'");
    
    
    
    channel.close();
    connection.close();
  }
  
  private static String getSeverity(String[] strings){
    if (strings.length < 1)
    	    return "info";
    return strings[0];
  }

  private static String getMessage(String[] strings){ 
    if (strings.length < 2)
    	    return "Hello World!";
    return joinStrings(strings, " ", 1);
  }
  
  private static String joinStrings(String[] strings, String delimiter, int startIndex) {
    int length = strings.length;
    if (length == 0 ) return "";
    if (length < startIndex ) return "";
    StringBuilder words = new StringBuilder(strings[startIndex]);
    for (int i = startIndex + 1; i < length; i++) {
        words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}

