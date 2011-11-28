import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class BP_machine extends UnicastRemoteObject implements machine {
	/* This is Kevs bit no laughing at my code :( */

	public static final String SERVICE_TYPE = "smart_hospital._tcp.local.";
	public static final String SERVICE_NAME = "BloodPressure";
	public static final int SERVICE_PORT = 1268;
	String P_ID = null;
	String patientWard = null;
	String Ward;
	public int patientID = 0;
	String bp_Result = "";
	protected MulticastSocket socket = null;
	protected InetAddress multicastAddress;
	boolean UDPin;

	public BP_machine(String location) throws RemoteException{
		Ward = location;
		UDPin = true;
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

		// Keep reading for ever
		while (UDPin == true) {
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String input = new String(packet.getData()).trim();
				String[] temp = input.split("_");
				this.P_ID = temp[0];
				this.patientWard = temp[1];
				// break. Start broadcasting
				if (Ward.equals(patientWard)) {
					System.out.println("Patient " + P_ID + " In Ward :" + patientWard);
					UDPin = false;
					startBroadcasting();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void Patient_ID(int p) {
		this.patientID = p;
	}

	public int getPatient_ID() {
		return patientID;
	}

	@Override
	public boolean has_Patient() {

		if (patientID != 0) {
			return true;
		}

		return false;
	}

	@Override
	public void completeTask() {
		// TODO How ever we are going to represent each machine

		int top_Number = 70 + (int) (Math.random() * ((160 - 70) + 1));
		int bottem_Number = 50 + (int) (Math.random() * ((100 - 50) + 1));

		String top_Result = Integer.toString(top_Number);
		String bottem_Result = Integer.toString(bottem_Number);

		this.bp_Result = top_Result + "/" + bottem_Result;

	}

	@Override
	public String getResults() {
		return bp_Result;

	}

	@Override
	public void toServer(String P, String W, String R) {
		// TODO Send.bp_results; or something like that
		// along with the patientID

	}

	public void startBroadcasting() {
		// You can specify the interface that services are registered (and
		// browsed
		// for) on; see the commented lines following.
		JmDNS jmdns;
		try {
			jmdns = JmDNS.create();
			ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME,SERVICE_PORT, 0, 0, "");
			jmdns.registerService(info);

			Registry registry = LocateRegistry.createRegistry(1099);
			Naming.rebind("BloodPressure", new BP_machine("Ward 3"));
			System.out.println("BP machine is ready");

			// System.out.println("Press enter to unregister and quit");
			// new BufferedReader(new InputStreamReader(System.in)).readLine();
			//
			// //bp_Result = completeTask();
			// //Unregister the service.

			jmdns.close();
			System.exit(0);
			System.out.println("Registered Service as " + info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Reposit Server failed: " + e);
		}
	}
	
	@Override
	public void unReg(JmDNS jmdns, ServiceInfo info) {
		jmdns.unregisterService(info);	
	}
	
	

	public static void main(String[] args) throws IOException {

		// UDP Receiver stuff
		BP_machine machine = new BP_machine("Ward 3");
		String multicastGroup = "230.0.0.1";
		String strMulticastPort = "4444";
		machine.UDPReceiver(multicastGroup, Integer.parseInt(strMulticastPort));
		Registry registry = LocateRegistry.createRegistry(2000);
		Naming.rebind("foundTest", (Remote) machine);
		
	}

	
}
