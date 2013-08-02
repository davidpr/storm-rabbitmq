import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

//to create json objects
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.io.*; //needs to be included for the stringwriter

public class StormSenderJSON {

  private static final String EXCHANGE_NAME = "stormexchange";

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    String severity = getSeverity(argv);
    String message = getMessage(argv);

	//put the string into a JSON text
	//String json="{ secondname:"+ message + " }";
	JSONObject obj = new JSONObject();
	//jsonObject.addProperty("name", "john");
	obj.put("name",message);
	StringWriter out = new StringWriter();
	JSONValue.writeJSONString(obj,out);
	String jsonText = out.toString();
	//System.out.println("I'm client and the text of my json object is: "+ jsonText +"\n");

    channel.basicPublish(EXCHANGE_NAME, severity, null, jsonText.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + severity + "':'" + jsonText + "'");

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

