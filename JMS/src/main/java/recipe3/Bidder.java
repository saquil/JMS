package recipe3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Bidder implements MessageListener {

    private TopicConnection tConnect = null;    
    private TopicSession tSession = null;
    private Topic topic = null;
    private double currentBid;

    public Bidder(String topiccf, String topicName, String bid) {    	
    	try {
    		currentBid = Double.valueOf(bid);
    		
    		// Connect to the provider and get the JMS connection
			Context ctx = new InitialContext();
			TopicConnectionFactory qFactory = (TopicConnectionFactory)
				ctx.lookup(topiccf);
			tConnect = qFactory.createTopicConnection();
			
			// Create the JMS Session
			tSession = tConnect.createTopicSession(
				false, Session.AUTO_ACKNOWLEDGE);

			// Lookup the request and response queues
			topic = (Topic)ctx.lookup(topicName);

			// Create the message listener
			TopicSubscriber subscriber = tSession.createSubscriber(topic);
			subscriber.setMessageListener(this);
			
            // Now that setup is complete, start the Connection
			tConnect.start();

			System.out.println("Waiting for bid requests...");
			
    	} catch (JMSException jmse) {
    		jmse.printStackTrace( ); 
    		System.exit(1);
        } catch (NamingException jne) {
            jne.printStackTrace( ); 
            System.exit(1);
        }
    }

    public void onMessage(Message message) {

    	try {
        	// Get the data from the message
        	BytesMessage msg = (BytesMessage)message;
        	double newBid = msg.readDouble();
        	
        	// If the bid is higher the current bid then 
        	//fold
        	if ((currentBid - newBid)>0) {
        		System.out.println("New bid = " + newBid + " - Follow");
        	} else {
        		System.out.println("New bid = " + newBid + " - Fold");
        	}

        	System.out.println("\nWaiting for bid updates...");

    	} catch (JMSException jmse) {
    		jmse.printStackTrace( ); 
    		System.exit(1);
    	} catch (Exception jmse) {
    		jmse.printStackTrace( ); 
    		System.exit(1);
    	}
    }
    
    private void exit() {
    	try {
    		tConnect.close( );
    	} catch (JMSException jmse) {
    		jmse.printStackTrace( );
    	}
    	System.exit(0);
    }

    public static void main(String argv[]) {
    	String topiccf = null;
    	String topicName = null;
    	String bid = null;
    	if (argv.length == 3) {
    		topiccf = argv[0];
    		topicName = argv[1];
    		bid = argv[2];
    	} else {
    		System.out.println("Invalid arguments. Should be: ");
    		System.out.println
               ("java Bidder factory topic bid");
    		System.exit(0);
    	}
      
    	Bidder borrower = new Bidder(topiccf, topicName, bid);
      
    	try {
    		// Run until enter is pressed
    		BufferedReader stdin = new BufferedReader
            	(new InputStreamReader(System.in));
    		System.out.println ("Bidder application started");
    		System.out.println ("Press enter to quit application");
    		stdin.readLine();
            borrower.exit();
    	} catch (IOException ioe) {
    		ioe.printStackTrace( );
    	}
   }
}
