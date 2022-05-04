package gov.nasa.cumulus.metadata.aggregator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;


import cumulus_message_adapter.message_parser.MessageParser;
import cumulus_message_adapter.message_parser.MessageAdapterException;


public class MetadataAggregatorHandler implements RequestHandler<String, String> {

	
	/**
     * Handle request coming from Lambda. Call Message Parser.
     * @param input - String input from lambda
     * @param context - Lambda context
     * @return output from message parser
     */
    public String handleRequest(String input, Context context) {
        MessageParser parser = new MessageParser();
        
        try
        {
        	return parser.RunCumulusTask(input, context, new MetadataAggregatorLambda());
            //return parser.HandleMessage(input, context, new MetadataAggregatorLambda(), null);
        }
        catch(MessageAdapterException e)
        {
            return e.getMessage();
        }
    }
    
    public void handleRequestStreams(InputStream inputStream, OutputStream outputStream, Context context) throws IOException, MessageAdapterException {
		
    	MessageParser parser = new MessageParser();
    	
    	try
        {
    		String input =IOUtils.toString(inputStream, "UTF-8");
    		context.getLogger().log(input);
        	String output = parser.RunCumulusTask(input, context, new MetadataAggregatorLambda());
        	
            //return parser.HandleMessage(input, context, new MetadataAggregatorLambda(), null);
        	outputStream.write(output.getBytes(Charset.forName("UTF-8")));
        	context.getLogger().log(output);
        	
        	if(output == null || output.equals("")){
        		//blank output?!
        		throw new MessageAdapterException("Null or Blank output after process ececution!");
        	}
        }
        catch(MessageAdapterException e)
        {
        	e.printStackTrace();
        	context.getLogger().log(e.getMessage());
        	throw e;
        }
	}
    
    
    
    
	
}
