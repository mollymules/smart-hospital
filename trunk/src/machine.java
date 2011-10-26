//Code for all the Machines to implement from
public interface machine {

	
	
	void UDPReceiver(String multicastGroup, int multiCastPort);
	
	//Takes in the Patient ID
	void Patient_ID(int p);
	
	//returns Patient
	int getPatient_ID();
	
	//True is has patient 
	boolean has_Patient();
	
	//Completes the specific job for each machine
	void  completeTask();
	
	//Returns the results
	String getResults();
	
	//Method for sending results to the sever
	void toServer();
	
	//Start ZeroConf Broadcasting
	void startBroadcasting();
	
	
	

	
	



}
