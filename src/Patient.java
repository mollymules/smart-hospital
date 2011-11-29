import java.util.LinkedList;
import java.io.*;
import java.net.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/*This file will represent a patient
 * The patient will be a discover service
 * once it connects with a machine, it checks to see if it needs its services
 * if it does it connects to the machine 
 * if not it moves on
 * the patient is static
 * should have a bed number perhaps to detect the machines as they are moving around
 * */

public class Patient implements Runnable {
	/* This is by Mary */
	private int patientID;
	private String location;// where the patient is in the hospital
	private LinkedList<String> tests;// tests the patient needs
	boolean noDeviceFound;
	protected MulticastSocket socket = null;
	protected InetAddress multicastAddress;
	protected int multicastPort;
	private JmDNS jmdns;
	public static final String SERVICE_TYPE = "smart_hospital._tcp.local.";

	public Patient(int patient_id, String ward) {
		patientID = patient_id;
		location = ward;
		noDeviceFound = true;
		tests = new LinkedList<String>();
		tests.add("XRay");
		multicastPort = 4443;
	}

	/*
	 * This is the simulated RFID tag. It broadcasts the patients ID and
	 * location every half a second.
	 */
	public void startBroadcast() {
		System.out.println("Patient "+ patientID);
		noDeviceFound = true;
		if(multicastPort < 4451){
			multicastPort += 1;
		}
		else{
			multicastPort = 4444;
		}	
		try {
			multicastAddress = InetAddress.getByName("230.0.0.1");
			socket = new MulticastSocket();
			socket.joinGroup(multicastAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (java.net.SocketException e) {
			System.out.println("Exception starting server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Continuously broadcast the patient id
		// until a device comes in range
		while (this.noDeviceFound) {
			send();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("broadcast has stopped");
	}

	public void send() {
		try {
			String msg = "" + patientID + "_" + location;
			byte[] buf = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					multicastAddress, multicastPort);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startListener() {
		try {
			jmdns = JmDNS.create();
			jmdns.addServiceListener(SERVICE_TYPE, new SampleListener());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LinkedList<String> getTests() {
		return tests;
	}
	public void addTest(String t) {
		tests.add(t);
	}

	class SampleListener implements ServiceListener {
		
		public void serviceAdded(final ServiceEvent event) {
			noDeviceFound = false;
			// the serviceResolved(...) method is called  with this line*/
			event.getDNS().requestServiceInfo(event.getType(), event.getName(),0);
					
		}
		
		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "."
					+ event.getType());
		}

		public void serviceResolved(ServiceEvent event) {
			// Display some information about the service.
			String testName = event.getInfo().getName();
			// which patient the machine is looking for:
			String patientRequest = event.getInfo().getTextString().trim();

			// if the patient isn't who the machine is looking for:
			if (!(Integer.toString(patientID).equals(patientRequest))) {
				System.out.println(patientID
						+ ": machine wasn't looking for you");
				run();
			} else {
				if (tests.contains(testName)) {
					System.out.println(patientID + " needs this test");
					String ip = event.getInfo().getHostAddress();
					int port = event.getInfo().getPort();
					try {
						Socket toServer = new Socket(ip, port);
						PrintWriter printer = new PrintWriter(toServer.getOutputStream(), true);
						printer.println(patientID);
						printer.flush();
						toServer.close();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				} else {
					System.out.println(patientID + " doesn't need this test");
				}
				run();
			}
		}
	}

	public void run() {
		this.startListener();
		this.startBroadcast();
	}
}
