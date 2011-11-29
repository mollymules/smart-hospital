

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;



public class PatientMeasuerment {
	private String serverHost;
	private int serverPort;
	private boolean foundserv=false;
	private String  SERVICENAME="hospitalserver";
	private Socket toServer;
	private JmDNS jmdns;
	/**
	 * The service type to browse for. This should be the same as the one you use
	 * to register services with.
	 */
	public static final String SERVICE_TYPE = "_smart_hospital_server._tcp.local.";
	/**
	 * Sets the client up with the server details.
	 * @param the_serverHost the server host name.
	 * @param the_serverPort the server port.
	 */
	public PatientMeasuerment() {
		
	

	}


	private void connectAvailServer() throws IOException{    	

		// Again, you can specify which network interface you would like to browse
		// for services on; see commented line.
		// final JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
		jmdns= JmDNS.create();
		// Work the magic: this is where the service listener is registered.
		jmdns.addServiceListener(SERVICE_TYPE, new SampleListener());

		System.out.println("I found a service");

		


	}
	
	private void useServiceAvailable(String host,int port){
		serverHost=host;
		serverPort=port;
		
		creatConnection(host,port);
		

		
		/*
		String message2="Measure\n" +
		"2\n" +
		"temperature\n" +
		"44\n" ;
		sendMessage(message2);*/
		
		String message="<?xml version=\"1.0\"?>\n" +
		 "<patient_measurement>\n"+
		 	"<patient_id>8</patient_id>\n"+
		 	"<blood_pressure> 65</blood_pressure>\n"+		 	
		 "</patient_measurement>\n"+
		"<xml_end>MessageEnd</xml_end>";
		sendMessage(message);
	
		
		//Sorry I finish with the service close service
		
			try {
				jmdns.close();
			} catch (Exception e) {System.out.println(e);	}		
        
	}
	
	
	private void creatConnection(String the_serverHost, int the_serverPort){
		// check if the Hostname or port Number are valid  
		// Create a connection to the server.
		
			try {
				toServer = new Socket(the_serverHost, the_serverPort);
			} catch (UnknownHostException e) {
				System.out.println("Sorry unable to connect to Server Host");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Could not get a socket from server");
				e.printStackTrace();
			}  
			   
	}
	
	
	public void sendMessage(String a_message) {
		PrintWriter out=null;
		BufferedReader in=null;
		try {

			// Wrap a PrintWriter round the socket output stream.
			// Read the javadoc to understand (1) the method arguments, and (2) why
			// we do this rather than writing to raw sockets.
			 out = new PrintWriter(toServer.getOutputStream(), true);
			 in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));

			// Write the message to the socket and wait for 1-sec to send the next one.
			
			 out.println(a_message);
			 out.flush();
			 String reply=null;			 
			 while((reply=in.readLine())!=null) System.out.println("Responce from Server" + reply);
					
						//Thread.sleep(100); // sleep every 1-sec i.e send message every 1-sec
			} catch (Exception e) {System.err.println("Sorry Iterator Interupted");}
			
            
        try{
			// tidy up
			out.close();
			in.close();
			toServer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SecurityException se) {
			se.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	private class SampleListener implements ServiceListener {
		public void serviceAdded(final ServiceEvent event) {
			System.out.println("Service added   : " + event.getName() + "." + event.getType());
			// The following line is required to get all information associated
			// with a service registration - not just the name and type - for
			// example, the port number and properties. Notification is sent to the
			// serviceResolved(...) method which the request has been completed.
			event.getDNS().requestServiceInfo(event.getType(), event.getName(), 0);
		}

		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "." + event.getType());

		}

		public void serviceResolved(ServiceEvent event) {
			// Display some information about the service.
			/*  System.out.println("Service resolved: " + event.getInfo().getName() + ", host: "
                 + event.getInfo().getHostAddress() + ", port: " + event.getInfo().getPort());*/
			System.out.println("Server :" + event.getInfo().getHostAddress());
			if(SERVICENAME.equalsIgnoreCase(event.getInfo().getName())){
				useServiceAvailable(event.getInfo().getHostAddress(),event.getInfo().getPort());
				

			}
		}
	}



	public static void main(String[] args){
		PatientMeasuerment client=new PatientMeasuerment();
		try {
			client.connectAvailServer();
		} catch (IOException e) {

			e.printStackTrace();
		}



	}


}
