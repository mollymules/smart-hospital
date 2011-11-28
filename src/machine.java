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
	
	//Method for sending results to the sever
	void toServer(String P, String W, String R);
	
	//Start ZeroConf Broadcasting
	void startBroadcasting(String P_ID);
	
	void unReg(JmDNS jmdns, ServiceInfo info);
	
	

	
	



}
