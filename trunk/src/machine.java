//Code for all the Machines to implement from
public interface machine {

	//All Method in an interface are public
	int P_ID = 0;
	
	void Patient_ID(int p);
	
	int getPatient_ID();
	
	boolean has_Patient();
	
	//Completes the specific job for each machine
	void  completeTask();
	
	//cross the task off the list?
	
	
	
	String getResults();
	
	
	void toServer();


}
