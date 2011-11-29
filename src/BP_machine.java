import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;


public class BP_machine extends UnicastRemoteObject implements machine {
	/* This is Kevs bit no laughing at my code :( */

	private static final String SERVICE_TYPE = "smart_hospital._tcp.local.";
	private static final String SERVICE_NAME = "BloodPressure";
	private static final int SERVICE_PORT = 1268;

	private String serverHost;
	private int serverPort;
	private boolean foundserv=false;
	private String  SERVICENAME="hospitalserver";
	
	
	private JmDNS jmdns;
	LinkedList<String> recentPatients;
	boolean UDPin;
	String UDPMultiAdd;
	int UDPMultiPort;

	private String patientWard;//where patient is
	private String myLocation;// where machine is
	private String patientID;
	
	private Socket toServer;
	String bp_Result;
	protected MulticastSocket socket;
	protected InetAddress multicastAddress;
	

	public BP_machine(String location, String add, int multiPort) throws RemoteException, MalformedURLException{
		String UDPMultiAdd = add;
		UDPMultiPort = multiPort;
		myLocation = location;
		UDPin = true;
		bp_Result = "";
		patientID =null;
		socket = null;
		recentPatients = new LinkedList<String>();
		patientWard = null;
		foundserv = false;
		SERVICENAME = "hospitalserver";
		UDPReceiver(add, multiPort);
	}

	public void UDPReceiver(String multicastGroup, int multiCastPort) {
		try {
			multicastAddress = InetAddress.getByName(multicastGroup);
			}
			catch(Throwable t) {
				System.out.println("Exception getting inetaddress for group:"+ multicastGroup);
			}
		try {
			// creates the multicast socket
			socket = new MulticastSocket(multiCastPort); 
			socket.joinGroup(multicastAddress);	 
			}
			catch (java.net.SocketException e) {
				System.out.println("Exception creating multicast socket and joining group: " + e.getMessage());
			}
		catch (IOException e) {     	 
			e.printStackTrace();		
		}
		// Keep reading forever
		while (UDPin == true) {
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String input = new String(packet.getData()).trim();
				String[] temp = input.split("_");
				this.patientID = temp[0];
				this.patientWard = temp[1];
				// break. Start broadcasting
				if (myLocation.equals(patientWard) && !recentPatients.contains(patientID)) {
					System.out.println("Patient " + patientID + " In myLocation :" + patientWard);
					UDPin = false;
					startBroadcasting();
				}else if (recentPatients.contains(patientID)){
					System.out.println("Patient " + patientID + " Has already been seen");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void completeTask(String patientID) {
		int top_Number = 70 + (int) (Math.random() * ((160 - 70) + 1));
		int bottem_Number = 50 + (int) (Math.random() * ((100 - 50) + 1));
		String top_Result = Integer.toString(top_Number);
		String bottem_Result = Integer.toString(bottem_Number);
		this.bp_Result = top_Result + "/" + bottem_Result;
		try {
			connectAvailServer(patientID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getResults() {
		return bp_Result;

	}

	public void startBroadcasting() {
		JmDNS jmdns;
		try {
			jmdns = JmDNS.create();
			ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME,SERVICE_PORT, 0, 0, ""+patientID);
			jmdns.registerService(info);
			recentPatients.add(patientID);
			ServerSocket server = new ServerSocket(SERVICE_PORT, 5);
			boolean inUse = true;
		      while (inUse) {
		             Socket socket = server.accept();
		             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		             String patient = in.readLine();
		             if(patient.equals(patientID)){
		            	 completeTask(patientID);
		             }
		             in.close();
		             socket.close();
		             unReg(jmdns, info);
		             inUse = false;
		       }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Machine Server failed: " + e);
		}
		UDPReceiver(UDPMultiAdd, UDPMultiPort);
	}
	
	public void unReg(JmDNS jmdns, ServiceInfo info) {
		jmdns.unregisterService(info);	
	}

	/**
	 * Sending to the database.
	 * 
	 * @throws IOException
	 */
	private void connectAvailServer(String patientID) throws IOException{

		// Again, you can specify which network interface you would like to browse
		// for services on; see commented line.
		// final JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
		jmdns= JmDNS.create();
		// Work the magic: this is where the service listener is registered.
		jmdns.addServiceListener(SERVICE_TYPE, new SampleListener());
	}
	private void useServiceAvailable(String host,int port) throws IOException{
		serverHost=host;
		serverPort=port;

		creatConnection(host,port);
		String message="<?xml version=\"1.0\"?>\n" +
		"<patient_measurement>\n"+
		"<patient_id>dd</patient_id>\n"+
		"<blood_pressure> "+ bp_Result+"</blood_pressure>\n"+		 	
		"</patient_measurement>\n"+
		"<xml_end>MessageEnd</xml_end>";
		sendMessage(message);
		//Sorry I finish with the service close service
		jmdns.close();		
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
			out = new PrintWriter(toServer.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
			// Write the message to the socket and wait for 1-sec to send the next one.
			out.println(a_message);
			out.flush();
			String reply=null;			 
			while((reply=in.readLine())!=null) System.out.println("Responce from Server" + reply);
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
	
	public static void main(String[] args) throws IOException {
		// UDP Receiver stuff
		BP_machine machine = new BP_machine("Ward 3","230.0.0.2", 4444);
		System.out.println("Awating Patient");
		
	}
}
