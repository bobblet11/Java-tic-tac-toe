import java.net.*;
import java.util.*;
import java.io.*;

/**
* The Server class is responsible for accepting incoming clients and allocating threads to them.
* 
* @author Benjamin Jun-jie Glover 3035962764
* @version 1.0
* @since 28-11-2023
* 
* @param serverSocket the socket of this server
* @param clients arrayList containing all connected clients
* @param clientWriters arrayList containing all connected printWriters
* 
*/
public class Server{
	
	/**
	 * main method runs server
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		Server server = new Server();
		server.operateServer();
	}
	
	ServerSocket serverSocket;
	ArrayList<Socket> clients;
	ArrayList<PrintWriter> clientWriters;
	
	/**
	 * Constructor method
	 */
	public Server()
	{
		System.out.println("[program log <SERVER>] STARTING SERVER");
		clients = new ArrayList<Socket>();
		clientWriters = new ArrayList<PrintWriter>();
		
		//open thread
		//start while loop to wait for clients
	}
	
	/**
	 * operateServer method will continuously wait for clients, and will open threads for new clients.
	 */
	public void operateServer()
	{
		//open socket
		try {
			serverSocket = new ServerSocket(6000);
			System.out.println("[program log <SERVER>] SERVER CREATED");
			while(true)
			{
				Socket incomingClient = serverSocket.accept();
				PrintWriter writer = new PrintWriter(incomingClient.getOutputStream(), true);
	
				
				if (clients.size() < 2)
				{
					int index = clients.size();
					writer.println("ACCEPTED:TRUE");
					clients.add(incomingClient);
					
					clientWriters.add(writer);
					
					//create new thread to handle client
					Runnable serverWorker = new ServerWorker(incomingClient, clients, clientWriters, index);
					Thread clientThread = new Thread(serverWorker);
					clientThread.start();
				}
				else
				{
					writer.println("ACCEPTED:FALSE");
					writer.close();
					writer = null;
					incomingClient.close();
				}

				//clientThread will terminate when either client disconnects or sends close protocol message
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("[program log <SERVER>] SERVER ERROR");
		}
	}
	
	
}
