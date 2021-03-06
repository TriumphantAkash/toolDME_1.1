import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CSrequestNodeListenerInstance extends Thread{

	Socket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	Message msg;
	ResourceProcess rProcess;
	CSrequestNodeListenerInstance(Socket sock, ResourceProcess rp) throws IOException{
		this.socket = sock;
		this.ois = new ObjectInputStream(sock.getInputStream());
		this.oos = new ObjectOutputStream(sock.getOutputStream());
		this.
		msg = new Message();
		rProcess = rp;
	}
	public void run(){
		
		
		
		while(true){

			try {
				msg = (Message)ois.readObject();
				//msg = (Message)ois.readUnshared();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				System.out.println("exception while reading resource message at Resource");
				e.printStackTrace();
			}
			
			rProcess.readCSMessage(msg);
			Message m = new Message();
			m.setMessage("csexit");
			try {
				oos.writeObject(m);
				oos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
	}
}
