import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

//Code for all the Machines to implement from
public interface machine {

	
	
	void UDPReceiver(String multicastGroup, int multiCastPort);
	
	//Completes the specific job for each machine
	void  completeTask(String patientID);
	
	String getResults();
	
	void unReg(JmDNS jmdns, ServiceInfo info);
	
	

	
	



}
