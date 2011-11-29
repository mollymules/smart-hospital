
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;



public class DoctorDisplay{

	
	private JmDNS jmdns;
	private Vector<ServerInfo> serverList;
	
	/**
	 * This is a private class to represent a server by its ip and port no
	 * @author bbradford
	 *
	 */
	private class ServerInfo{
		
		private String ip;
		private int port;
		
		/**
		 * Stores the ip and port no
		 * @param ipAddress
		 * @param portNo
		 */
		public ServerInfo(String ipAddress, int portNo){
			this.ip = ipAddress;
			this.port = portNo;
		}
		
		/**
		 * Returns the port no of the server
		 * @return
		 */
		public int getPort(){
			return port;
		}
		
		/**
		 * Returns the IP address of the server
		 * @return
		 */
		public String getIp(){
			return ip;
		}
		public String toString(){
			return "IP: "+ip+" Port: "+port;
		}
	}
	
	
	
	
	/**
	 * The service type to browse for. This should be the same as the one you use
	 * to register services with.
	 */
	public static final String SERVICE_TYPE = "_smart_hospital_server._tcp.local.";
	
	private String  SERVICENAME="hospitalserver";
	
	

	/**
	 * Default constructor
	 * On creation of the class it will look for all servers offering Service type and have the same service name.
	 * It then saves theses services info to a vector.
	 * When a message is to be sent it will use the first server in the vector, if that server is unavailable
	 * it use the second one and so on.
	 * @throws IOException
	 */
	public DoctorDisplay() throws IOException{
		
		serverList = new Vector<DoctorDisplay.ServerInfo>();
		
		jmdns = JmDNS.create();
		// Work the magic: this is where the service listener is registered and it looks for the service.
		//when the service is found its info is stored for later use using SaveHostInfo()
		jmdns.addServiceListener(SERVICE_TYPE, new SampleListener());

		System.out.println("Looking for a service");
		
	}
	
	/**
	 * this method saves the host info to global variables for use when connecting the server later
	 */
	private void saveHostInfo(String ipAddress, int port){
		
		//make a serverInfo object with all the info
		ServerInfo temp = new ServerInfo(ipAddress, port);
		
		//check to see if the server is there allready
		for(int i = 0; i < serverList.size(); i++){
			if(serverList.get(i).getIp().equals(temp.getIp()) && serverList.get(i).getPort() == temp.getPort()){
				System.out.println("not saved");
				return;
			}
		}
		
		// else the server is not in the list so add the object to the list
		serverList.add(temp);
		
		System.out.println("The Server: "+ipAddress+" : "+port+" is added to the vector of servers");

		
		System.out.println("Servers: ");
		for(int i = 0; i < serverList.size(); i++){
			System.out.println(serverList.get(i));
		}
		
	}
	
	/**
	 * This method simply closes the jmdns connection so it will no longer
	 * keep looking for servers to connect to.
	 */
	public void closeJMDNS(){
		jmdns.close();
		
	}
	
	/**
	 * This method will close the current jmdns and reopen
	 * a new one to search for new services
	 * @throws IOException 
	 */
	public void closeAndReopenJMDNS() throws IOException{
		jmdns.close();
		jmdns = JmDNS.create();
		// Work the magic: this is where the service listener is registered and it looks for the service.
		//when the service is found its info is stored for later use using SaveHostInfo()
		jmdns.addServiceListener(SERVICE_TYPE, new SampleListener());
	}
	
	
	/**
	 * This method takes a string from the gui and sends it to
	 * the server. Then returns the reply from the server as 
	 * every message sent has a reply.
	 * @param message
	 * @return The reply from the server
	 */
	public String sendMessageFromGui(String message){
		
		//get a connection to a serve, the first server of the list
		//return null if there is no conection
		Socket toServer = creatConnectionToServer();

		
		//then set up to send the message
		PrintWriter out=null;
		BufferedReader in=null;
		String reply = null;
		String totalReply = "";
		try {
			 out = new PrintWriter(toServer.getOutputStream(), true);
			 in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));

			 //send the message to the server
			 out.println(message);
			 out.flush();
			
			 System.out.println("I have sent the message to the server");
			
			 //and treat the reply
			 while((reply=in.readLine())!=null) {
				 totalReply=totalReply+"\n"+reply;
			 }
			
			 System.out.println("finished reciving mesage from the server");
			 
					
		} catch (Exception e) {System.err.println("There are no more servers to conect to!");}
			
         
			
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
		
		return totalReply;	
	}
	

	
	/**
	 * This method conects to the server
	 * If the server is no longer available it will use another one from the list of servers
	 * that offer the same service. 
	 * @param hostAddress
	 * @param port
	 * @return
	 */
	private Socket creatConnectionToServer() {
		// Create a connection to the server and return this connection.
		Socket toServer = null;
		toServer = new Socket();
		
		
//to do		
		//if this fails then try the other list of ip and port that are not made yet
		try {
			//try to connect to the serverListIndex server on the list
			toServer.connect(new InetSocketAddress(serverList.get(0).getIp(), serverList.get(0).getPort()), 2000);
			
			System.out.println(" KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
			
		} catch (Exception e) {
			//this server is now gone so remove it
			//but close the socket first
			try {
				toServer.close();
				closeAndReopenJMDNS();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			serverList.remove(0);
			
			// and try another one if there is another one.
			if(serverList.size() > 0){
				//we know there is another server in the list
				System.out.println("The curent server failed Moving on to server: "+serverList.get(0).toString());
				toServer = creatConnectionToServer();
			}else{
				//we have reached the end of the list or there was no other servers. this should never happen in 
				//the hospital providing the hospital is covered sufficiently.
				System.out.println("No More servers to conect to!!!!!!!!!!!!!!!!!!!!");				
			}
			
		
			
		}
		
		return toServer;
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

		
		
		//This is only called if the server closes the socket, not if the server just dies 
		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "." + event.getType());

		}

		
		public void serviceResolved(ServiceEvent event) {
			// Display some information about the service.
			/*  System.out.println("Service resolved: " + event.getInfo().getName() + ", host: "
                 + event.getInfo().getHostAddress() + ", port: " + event.getInfo().getPort());*/
			//System.out.println("Server :" + event.getInfo().getHostAddress());
			
			
			//if the service that is found is the service we are looking for save its ip and port
			if(event.getInfo().getName().contains(SERVICENAME)){
				saveHostInfo(event.getInfo().getHostAddress(), event.getInfo().getPort());		
			}
		}
	}



}