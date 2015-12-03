/*
Cian butler - 13373596
Eanna Byrne - 13763861
Lorcan Boyle - 11411278
Daire O'Bruachail - 13479738
*/

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
		}catch(IOException b){
			System.out.println("Error creating printwriter or bufferedreader");
		}
	}

	public void run(){
		try{
			String user = in.readLine();
			usernames.add(user);
			System.out.println(user + " has connected.");
			String inputLine;
			buffer.add(user + " Connected");

			//add the next input to the buffer
			while ((inputLine = in.readLine()) != null) {
				buffer.add(user + ": " + inputLine);
				if (out.checkError()){
					buffer.add(user + " has disconnected...");
					System.out.println("ClientReader shuting down");
					return;//Shut down this thread, remove the socket and username
				}
			}
		}catch(IOException c){
			System.out.println("ClientReader shuting down");
			return;
		}catch(InterruptedException e) {
			System.out.println("Error running ClientReader");
		}
	}
}

class ClientWriter implements Runnable{
	private Buffer buffer;
	private Vector<Socket> clients;
	private Vector<String> usernames;

	public ClientWriter(Buffer b, Vector<Socket> c, Vector<String> u){
		this.buffer = b;
		this.clients = c;
		this.usernames = u;
	}

	public void run(){
		try{
			while(true)
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
		}catch(InterruptedException c){
			System.out.println("ClientWriter shuting down");
			return;
		}catch(IOException e) {
			System.out.println("Error running ClientWriter");
		}
	}
}

public class ChatServer
{
	public static void main(String [] args)
	{
		int port = 7777;

		Buffer buffer = new Buffer(10);
		Vector<Socket> clients = new Vector<Socket>();
		Vector<String> user = new Vector<String>();
		Vector<Thread> readers = new Vector<Thread>();

		Thread writer = new Thread(new ClientWriter(buffer, clients, user));
		writer.start();
		try{
			ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(null));
			while(true) //accept new connections, make a producer thread for them, remember their socket for consumer
			{
				Socket clientSocket = server.accept();
				if(clientSocket!=null){
					//only add temp to the array if there is a socket to connect
					Thread temp = new Thread(new ClientReader(clientSocket, buffer, user));
					temp.start();
					readers.add(temp);
					clients.add(clientSocket);
				}
				for(int i = 0; i < clients.size(); i++){
					//check to see if there are any closed clients
					Socket checkOpen = clients.elementAt(i);
					PrintWriter out = new PrintWriter(checkOpen.getOutputStream(), true);
					//checking read/write streams is the most reliable way of checking connection state
					if(out.checkError()){
						checkOpen.close();
						clients.remove(i);
						user.remove(i);
						(readers.elementAt(i)).interrupt();
						(readers.elementAt(i)).join();
						readers.remove(i);
					}
				}
				if(clients.size() == 0){
					writer.interrupt();
					writer.join();
				}
			}
		}catch(IOException b){
			System.out.println("Error in main");
		}catch(InterruptedException a){
			System.out.println("Interrupt caught in main");
		}
		System.out.println("Goodbye");
	}
}
