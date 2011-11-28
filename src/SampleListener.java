import java.io.IOException;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

class SampleListener implements ServiceListener {
	
		private String  SERVICENAME="hospitalserver";
		
		private void useServiceAvailable(String host,int port){
			serverHost=host;
			serverPort=port;
			
			creatConnection(host,port);
			
			/*
			String message="Registration\n" +
			"patient_id"+ " "+"002\n" +
			"patient_name"+ " "+"\"Sylvester\"\n" +
			"temperature"+ " "+"33" ;
			sendMessage(message);*/
			
		
			String message="<?xml version=\"1.0\"?>\n" +
			 "<patient_registration>\n"+
			 	"<patient_name>David Salt</patient_name>\n"+
			 	"<age>12</age>\n"+
			 "</patient_registration>\n"+
			"<xml_end>MessageEnd</xml_end>";
			sendMessage(message);
			
			
			// I finish with the service close service
			
				try {
					jmdns.close();
				} catch (IOException e) {System.out.println(e);	}		
	        
		}

		public void serviceAdded(final ServiceEvent event) {
			System.out.println("Service added   : " + event.getName() + "." + event.getType());
			// The following line is required to get all information associated
			// with a service registration - not just the name and type - for
			// example, the port number and properties. Notification is sent to the
			// serviceResolved(...) method which the request has been completed.
			event.getDNS().requestServiceInfo(event.getType(), event.getName(), 0);
		}

		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "." + event.getType());

		}

		public void serviceResolved(ServiceEvent event) {
			// Display some information about the service.
			/*  System.out.println("Service resolved: " + event.getInfo().getName() + ", host: "
                 + event.getInfo().getHostAddress() + ", port: " + event.getInfo().getPort());*/
			System.out.println("Server :" + event.getInfo().getHostAddress());
			if(SERVICENAME.equalsIgnoreCase(event.getInfo().getName())){
				useServiceAvailable(event.getInfo().getHostAddress(),event.getInfo().getPort());
				System.out.println("I found a service");

			}
		}
}