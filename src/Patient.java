import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
	}

	/*
	 * This is the simulated RFID tag. It broadcasts the patients ID and
	 * location every half a second.
	 */
	public void startBroadcast() {
		multicastPort = 4444;
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
			System.out.println(msg);
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
			System.out.println(testName);
			String patientRequest = event.getInfo().getTextString().trim();
			System.out.println(patientRequest);
			if(!(Integer.toString(patientID).equals(patientRequest))){
				System.out.println(patientID + ": machine wasn't looking for you");
				noDeviceFound = true;
				startBroadcast();
				jmdns.close();
				startListener();
			}
			/*
			 * System.out.println("Service resolved: " + testName + ", host: " +
			 * event.getInfo().getHostAddress() + ", port: " +
			 * event.getInfo().getPort());
			 
			machine foundTest = null;*/
			try {
				machine aMachine = (machine) Naming.lookup("//localHost/"+testName );
				aMachine.completeTask();
				//System.out.println("storing data for sensor " + sensorID);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				/*HERE:
				 * I would use a linked list to store the readings
				 * that weren't sent 
				 * when the exception is lifted,
				 * the list would be sent to the repository and
				 * added to the current list for the sensor*/
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		
			if (tests.contains(testName)) {
				System.out.println(patientID +" wants this test");
				//foundTest.completeTask();
			} else {
				System.out.println(patientID +" doesn't need this test");
				//foundTest.unReg(jmdns, event.getInfo());
				noDeviceFound = true;
				startBroadcast();
				jmdns.close();
				startListener();	
			}
		}
	}

	public void run() {
		this.startListener();
		this.startBroadcast();
	}

	public static void main(String[] args){
		Patient a = new Patient(1, "Ward 1");
		Patient b = new Patient(2, "Ward 3");
		b.tests.add("BloodPressure");
		Thread j = new Thread(a);
		Thread k = new Thread(b);
		j.start();
		k.start();

	}
}
