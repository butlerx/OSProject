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


class ServerThread implements Runnable{
	private Socket socket = null;
	private Buffer buffer;
	PrintWriter out;
	BufferedReader in;
	
	public ServerThread(Socket socket, Buffer buffer){ //this is a producer
		this.socket = socket;
		this.buffer = buffer;
		try{
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(IOException b){System.out.println("Error creating printwriter or bufferedreader");}
	}
	
	public void write(String output){
		out.println(output);
	}
	
	public void run(){
		try{
			String inputLine;
			out.println("Connected");

			while ((inputLine = in.readLine()) != null) {
				buffer.add(inputLine);
				if (inputLine.equals("Quit"))
					break;
			}
		} catch(IOException|InterruptedException e) {System.out.println("Error running serverthread");}
	}
}

class Consumer implements Runnable{
	private Buffer buffer;
	private ArrayList<ServerThread> threadList;
	boolean running = true;
	
	public Consumer(Buffer buffer, ArrayList<ServerThread> threadList){
		this.buffer = buffer;	
		this.threadList = threadList;
	}
	
	public void run(){
		try{
			while(running)
			{
				String message = buffer.remove();
				for(ServerThread thread :threadList)
					thread.write(message);
				//if there is a message
				//extract message
				//for each thread
				//send the message
			}
		}catch(InterruptedException e) {System.out.println("Error running consumer");}
	}
}

public class ChatServer
{
	public static void main(String [] args)
	{
		int portNumber = 4444; //temporary port number til I figure shit out
		boolean running = true;
		
		Buffer buffer = new Buffer(10);
		ArrayList<ServerThread> threadList = new ArrayList<>();
		
		Thread consumer = new Thread(new Consumer(buffer, threadList)).start();
		
		ServerSocket server = new ServerSocket(portNumber);
		while(running)
		{
			Socket clientSocket = server.accept();
			threadList.add(new Thread(new ServerThread(clientSocket, buffer)).start());
		}
		
		//use an arraylist
	}
}