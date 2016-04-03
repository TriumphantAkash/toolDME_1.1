import java.io.IOException;
import java.util.concurrent.BlockingQueue;


public class ClientListenerWriter extends Thread {
	
	BlockingQueue<String> b;
	String message;
	public ClientListenerWriter(BlockingQueue<String> b)
	{
		this.b = b;
		message = new String();
	}
	
	public void run()
	{
		while(true)
		{
			try {
				message = b.take();
				String[] split = message.split("\\s+");
				int sendNode = Integer.parseInt(split[2]);
				SocketConnectionServer.clientOS.get(sendNode).writeBytes(message);
				SocketConnectionServer.clientOS.get(sendNode).writeBytes("\n");

				SocketConnectionServer.clientOS.get(sendNode).flush();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
