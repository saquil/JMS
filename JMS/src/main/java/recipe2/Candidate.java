package recipe2;

import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;
import javax.jms.*;
import javax.naming.*;

public class Candidate {
	
    private QueueConnection qConnect = null;    
    private QueueSession qSession = null;
    private Queue responseQ = null;
    private Queue requestQ = null;

    public Candidate(String queuecf, String requestQueue, 
    		        String responseQueue) {    	
    	try {
    		// Connect to the provider and get the JMS connection
			Context ctx = new InitialContext();
			QueueConnectionFactory qFactory = (QueueConnectionFactory)
				ctx.lookup(queuecf);
			qConnect = qFactory.createQueueConnection();
			
			// Create the JMS Session
			qSession = qConnect.createQueueSession(
				false, Session.AUTO_ACKNOWLEDGE);

			// Lookup the request and response queues
			requestQ = (Queue)ctx.lookup(requestQueue);
			responseQ = (Queue)ctx.lookup(responseQueue);

            // Now that setup is complete, start the Connection
			qConnect.start();
			
    	} catch (JMSException jmse) {
    		jmse.printStackTrace( ); 
    		System.exit(1);
        } catch (NamingException jne) {
            jne.printStackTrace( ); 
            System.exit(1);
        }
    }

    private void sendsalaryRequest(double salary, double expAmt) {
    	try {
        	// Create JMS message
        	MapMessage msg = qSession.createMapMessage();
        	msg.setDouble("Salary", salary);
        	msg.setDouble("Years� experience", expAmt);
        	msg.setJMSReplyTo(responseQ);

        	//set the message expiration to 30 seconds
        	msg.setJMSExpiration(new Date().getTime() + 30000);
        	
        	// Create the sender and send the message
			QueueSender qSender = qSession.createSender(requestQ);
        	qSender.send(msg);
        	
        	// Wait to see if the salary request was accepted or declined
        	String filter = 
        		"JMSCorrelationID = '" + msg.getJMSMessageID() + "'";
			QueueReceiver qReceiver = qSession.createReceiver(responseQ, filter);
        	TextMessage tmsg = (TextMessage)qReceiver.receive(30000);
        	if (tmsg == null) {
            	System.out.println("Recruiter not responding");
        	} else {
        		System.out.println("Salary request was " + tmsg.getText());
        	}
        	
    	} catch (JMSException jmse) {
    		jmse.printStackTrace( ); 
    		System.exit(1);
    	}
    }
    
    private void exit() {
    	try {
    		qConnect.close( );
    	} catch (JMSException jmse) {
    		jmse.printStackTrace( );
    	}
    	System.exit(0);
    }

    public static void main(String argv[]) {
    	String queuecf = null;
    	String requestq = null;
    	String responseq = null;
    	if (argv.length == 3) {
    		queuecf = argv[0];
    		requestq = argv[1];
    		responseq = argv[2];
    	} else {
    		System.out.println("Invalid arguments. Should be: ");
    		System.out.println
               ("java Candidate factory requestQueue responseQueue");
    		System.exit(0);
    	}
      
    	Candidate candidate = new Candidate(queuecf, requestq, responseq);
      
    	try {
    		// Read all standard input and send it as a message
    		BufferedReader stdin = new BufferedReader
            	(new InputStreamReader(System.in));
    		System.out.println ("Candidate Application Started");
    		System.out.println ("Press enter to quit application");
    		System.out.println ("Enter: Salary, Years experience");
    		System.out.println("\ne.g. 50000, 3");

    		while (true) {
        		System.out.print("> ");
    			
        		String salaryRequest = stdin.readLine();
        		if (salaryRequest == null || salaryRequest.trim().length( ) <= 0) {
        			candidate.exit();
        		}
            
        		// Parse the deal description
        		StringTokenizer st = new StringTokenizer(salaryRequest, ",") ;
        		double salary = 
        			Double.valueOf(st.nextToken().trim()).doubleValue( );
        		double expAmt = 
        			Double.valueOf(st.nextToken().trim()).doubleValue( );

        		candidate.sendsalaryRequest(salary, expAmt);
    		}
    	} catch (IOException ioe) {
    	  ioe.printStackTrace( );
    	}
    }
}
