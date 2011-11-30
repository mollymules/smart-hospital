import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Hashtable;


public class Hospital {
	BP_machine bp1;
	Hashtable<Integer, String> allTests;
	
	public Hospital(int numPatients){
		allTests = new Hashtable<Integer, String>();
		allTests.put(0, "BloodPressure");
		allTests.put(1, "XRay");
		allTests.put(2, "CatScan");
		allTests.put(3, "Temp");
		for(int i = 0; i< numPatients; i++){
			createPatient(i);
		}
			try {
				bp1 = new BP_machine("Ward 1","230.0.0.1", 4444);
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread t = new Thread(bp1);
			t.start();
			System.out.println("macjhine");
	}
	
	public void createPatient(int i){
		String ward = "Ward "+((int)(Math.random()*3)+1);
		Patient m = new Patient(i, ward);
		m.addTest(addTest());
		Thread k = new Thread(m);
		k.start();
		System.out.println("Patient "+i+" is in "+ ward +" and needs test "+m.getTests().toString());
	}
	
	public String addTest(){
		String test = allTests.get((int)(Math.random()*allTests.size()));
		return test;
	}
	
	public static void main(String[] args){
		Hospital hos = new Hospital(10);
	}

}
