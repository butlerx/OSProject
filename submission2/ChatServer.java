class Buffer{
	private boolean roomAvailable;
	private boolean dataAvailable;
	private String[][] buffer = new String[10][];
	
	public Buffer(){
		
	}
	
	public add(){
		
	}
	
	public remove(){
		
	}
}


class ServerThread implements Runnable{
	private Socket socket = null;
	private Buffer buffer;
	PrintWriter out;
	BufferedReader in;
	
	public ServerThread(Socket socket, Buffer buffer){ //this is a producer
		this.socket = socket;
		this.buffer = buffer;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public write(String output){
		out.println(output);
	}
	
	public void run(){
		try{
			String inputLine;
			out.println("Connected");

			while ((inputLine = in.readLine()) != null) {
				buffer.add(inputLine);
				if (inLine.equals("Quit"))
					break;
			}
		} catch(IOException e) {System.out.println("Error running serverthread");}
	}
}

class Consumer implements Runnable{
	private Buffer buffer;
	
	public Consumer(Buffer buffer){
		this.buffer = buffer;	
	}
	
	public void run(){
		while(running)
		{
			//if there is a message
			//extract message
			//for each thread
			//send the message
		}
	}

public class ChatServer
{
	public static void main(String [] args)
	{
		int portNumber = 4444; //temporary port number til I figure shit out
		boolean running = true;
		
		Buffer buffer = new Buffer();
		
		ServerSocket server = new ServerSocket(portNumber)
		while(running)
		{
			Socket clientSocket = serverSocket.accept();
			new Thread(new ServerThread(clientSocket, buffer)).start();
		}
	}
}