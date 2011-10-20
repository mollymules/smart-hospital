import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;


public class BP_machine implements machine {
	/* This is Kevs bit no laughing at my code :( */
	
	public static final String SERVICE_TYPE = "_BloodPressure._udp.local.";

	public static final String SERVICE_NAME = "a_BloodPressure";

	public static final int SERVICE_PORT = 1268;
	
	private int patientID = 0;
	String bp_Result = "";
		
	BP_machine (int P_ID){
		this.patientID = P_ID;
	}
	
	public void Patient_ID(int p){
		this.patientID = p;
	}
	
	public int getPatient_ID(){
		return patientID;
	}
		
	
	@Override
	public boolean has_Patient() {
		
		if(patientID != 0){
			return true;
		}
		
		return false;
	}

	@Override
	public void completeTask() {
		// TODO How ever we are going to represent each machine
	
		int top_Number = 70 + (int)(Math.random() * ((160 - 70) + 1));
		int bottem_Number = 50 + (int)(Math.random() * ((100 - 50) + 1));
		
		String top_Result = Integer.toString(top_Number);
		String bottem_Result = Integer.toString(bottem_Number);
		
		this.bp_Result = top_Result+ "/"+ bottem_Result;
	
	}
	
	@Override
	public String getResults() {
		return bp_Result;
		
	}

	@Override
	public void toServer() {
		// TODO Send.bp_results; or something like that
		// along with the patientID
		
	}

	public static void main(String[] args) throws IOException {
		
		 JmDNS jmdns = JmDNS.create();
	      // JmDNS jmdns = JmDNS.create(InetAddress.getByName("192.168.1.102"));
	      // JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

	      // Services are registered using ServiceInfo objects. Note that there are
	      // several create(...) methods for ServiceInfo objects. These mostly
	      // differ in how you specify properties that can be associated with
	      // service (the last parameter in the following line). For example, it is
	      // possible to provide a small hashtable which will be attached to the
	      // registration and is advertised with the service info.
	      
		 
		 ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, SERVICE_PORT, 0, 0,"a_property=some_value");
	      jmdns.registerService(info);
	      System.out.println("Registered Service as " + info); // note that the
	                                                           // service name may
	                                                           // have changed if a
	                                                           // service with that
	                                                           // name was already
	                                                           // registered.
	      // Wait for a keystroke before unregistering and quitting.
	      System.out.println("Press enter to unregister and quit");
	      new BufferedReader(new InputStreamReader(System.in)).readLine();
	      // Unregister the service.
	      jmdns.unregisterService(info);
	      
	      /*NEED TO CHANGE HERE..
	       * SWITCH TO BE A DISCOVERABLE OBJECT 
	       * THEN SEND RESULTS, 
	       *SWAITCH BACK TO BE A REGISTAR DEVICE 
	       */
	      
	      jmdns.close();
	      System.exit(0);
	
	
	
	
	
	
	}
	
	


	
}
