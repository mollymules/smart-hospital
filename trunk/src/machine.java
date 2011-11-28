import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

//Code for all the Machines to implement from
public interface machine {

	
	
	void UDPReceiver(String multicastGroup, int multiCastPort);
	
	//Takes in the Patient ID
	void Patient_ID(String p);
	
	//returns Patient
	String getPatient_ID();
	
	//True is has patient 
	boolean has_Patient();
	
	//Completes the specific job for each machine
	void  completeTask();
	
	//Returns the results
	String getResults();
	
	
	void unReg(JmDNS jmdns, ServiceInfo info);
	
	

	
	



}
