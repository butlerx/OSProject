import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

class Buffer{
	private boolean roomAvailable = true;
	private boolean dataAvailable = false;
	private int occupied = 0;
	private int size;
	private int nextIn = 0;
	private int nextOut = 0;
	private String[] buffer;
	
	public Buffer(int size){
		this.size = size;
		buffer = new String[size];
	}
	
	public synchronized void add(String data) throws InterruptedException{
		while(!roomAvailable)
			wait();
		buffer[nextIn] = data;
		nextIn = (nextIn + 1)%size;
		dataAvailable = true;
		occupied++;
		if(occupied == size)
			roomAvailable = false;
		notifyAll();
	}
	
	public synchronized String remove() throws InterruptedException{
		while(!dataAvailable)
			wait();
		String data = buffer[nextOut];
		nextOut = (nextOut + 1)%size;
		roomAvailable = true;
		occupied--;
		if(occupied == 0)
			dataAvailable = false;
		notifyAll();
		return data;
	}
}


class ClientReader implements Runnable{
	private Socket socket = null;
	private Buffer buffer;
	private Vector<String> usernames;
	PrintWriter out;
	BufferedReader in;
	
	public ClientReader(Socket socket, Buffer buffer, Vector<String> usernames){ //this is a producer
		this.socket = socket;
		this.buffer = buffer;
		this.usernames = usernames;
		try{
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(IOException b){System.out.println("Error creating printwriter or bufferedreader");}
	}

	public void run(){
		try{
			String user = in.readLine();
			usernames.add(user);
			System.out.println(user + " has connected.");
			String inputLine;
			out.println(user + " Connected");

			//add the next input to the buffer
			while ((inputLine = in.readLine()) != null) {
				buffer.add(user + ": " + inputLine);
				if (inputLine.equals("Quit")){
					//Shut down this thread, remove the socket and username
				}
			}
		} catch(IOException|InterruptedException e) {System.out.println("Error running ClientReader");}
	}
}

class ClientWriter implements Runnable{
	private Buffer buffer;
	private Vector<Socket> clients;
	private Vector<String> usernames;
	boolean running = true;
	
	public ClientWriter(Buffer buffer, Vector<Socket> clients, Vector<String> usernames){
		this.buffer = buffer;	
		this.clients = clients;
		this.usernames = usernames;
	}
	
	public void run(){
		try{
			while(running)
			{
				//if there is a message
				//extract message
				//for each socket, make a printwriter to send the message
				String message = buffer.remove();
				for(int i = 0; i < clients.size(); i++){
					Socket temp = clients.elementAt(i);
					PrintWriter out = new PrintWriter(temp.getOutputStream(), true);
					out.println(message);
				}
				System.out.println(message); //serverside record
			}
		}catch(InterruptedException | IOException e) {System.out.println("Error running ClientWriter");}
	}
}

public class ChatServer
{
	public static void main(String [] args)
	{
		int portNumber = 7777;
		boolean running = true;
		
		Buffer buffer = new Buffer(10);
		Vector<Socket> clients = new Vector<Socket>();
		Vector<String> usernames = new Vector<String>();
		
		Thread reader = new Thread(new ClientWriter(buffer, clients, usernames));
		reader.start();
		try{
			ServerSocket server = new ServerSocket(portNumber, 0, InetAddress.getByName(null));//localhost
			while(running) //accept new connections, make a producer thread for them, remember their socket for consumer
			{
				Socket clientSocket = server.accept();
				Thread temp = new Thread(new ClientReader(clientSocket, buffer, usernames));
				temp.start();
				clients.add(clientSocket);
			}
		}catch(IOException b){System.out.println("Error in main");}
	}
}