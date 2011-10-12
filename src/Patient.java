import java.util.LinkedList;
import java.io.*;
import java.net.*;
import java.util.*;	



public class Patient {
	/*This is by Mary*/
	private int patientID;
	private LinkedList<String> tests;
	protected MulticastSocket socket = null; 
    protected InetAddress multicastAddress;
    protected int multiCastPort;
	
	public Patient(){
		patientID = 3;
	}
	public LinkedList<String> getTests(){
		return tests;
		
	}


    public void UDPSender(String multicastGroup,  int multiCastPort) {

	this.multiCastPort = multiCastPort;

	try {
	  multicastAddress = InetAddress.getByName(multicastGroup);
	}
	catch(Throwable t) {
	}


	try {
 	  socket = new MulticastSocket();
	  socket.joinGroup(multicastAddress); 
	}
	catch (java.net.SocketException e) {
	  System.out.println("Exception starting server: " + e.getMessage());
	}
	catch (IOException e) {     	 
	        e.printStackTrace();		
    	}	
	
    }

    public void send(String msg) {
    	try {					
		//byte[] buf = new byte[256];			
		byte[] buf = msg.getBytes();

		// create the packet to wrap the msg data
		DatagramPacket packet = new DatagramPacket(buf, buf.length, 			multicastAddress, multiCastPort);
	     
		socket.send(packet);
	}
	catch (IOException e) {     	 
	    e.printStackTrace();	
	    // TO-DO: improve by handling this exception	
    	}
    }

}
