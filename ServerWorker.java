import java.io.*;
import java.net.*;
import java.util.ArrayList;



/**
* The Server class is responsible for accepting incoming clients and allocating threads to them.
* 
* @author Benjamin Jun-jie Glover 3035962764
* @version 1.0
* @since 28-11-2023
* 
* @param clientSocket the socket to the server
* @param clients arrayList containing all connected clients
* @param writers arrayList containing all connected printWriters
* @param writer the PrintWriter to the client that this thread is responsible for
* @param streamReader the InputStreamReader to the client that this thread is responsible for
* @param reader the BufferedReader to the client that this thread is responsible for
* @param index index of this client in the arrayList clients
* @param firstPlayerName the temporary storage for the clients name
* @param needToSendName 
* 
*/
public class ServerWorker implements Runnable{
	//socket variables
	private Socket clientSocket;
	ArrayList<Socket> clients;
	ArrayList<PrintWriter> writers;
	
	//io
	private PrintWriter writer;
	private InputStreamReader streamReader;
	private BufferedReader reader;
	private int index;
	private String firstPlayerName;
	private boolean needToSendName = false;
	
	/**
	 * Constructor method
	 * @param sock socket to the client
	 * @param clients reference to the arrayList of all clients
	 * @param writers reference to the arrayList of all printWriters
	 * @param index index of this client
	 */
	public ServerWorker(Socket sock, ArrayList<Socket> clients, ArrayList<PrintWriter> writers, int index)
	{
		this.clientSocket = sock;
		this.clients = clients;
		this.writers = writers;
		this.index = index;
		this.writer = writers.get(index);
		System.out.println("[program log <SERVER>] SERVER WORKER CREATED");
		
	}
	
	/**
	 * implemented run method will continuously read and relay messages from the client to other clients.
	 */
	public void run()
	{
		try
		{
			streamReader = new InputStreamReader(this.clientSocket.getInputStream());
			reader = new BufferedReader(streamReader);		
			encodeAndSendMessage("SERVER: CLIENT HAS CONNCETED", "PRINT");
			
			if (index == 0)
			{
				//is the first player
				encodeAndSendMessage("1", "INIT");
			}
			else 
			{
				encodeAndSendMessage("2", "INIT");
			}
			
			
			
			
			while (true)
			{
				if (clients.size()==2 && needToSendName == true)
				{
					System.out.println("SENDING THE NAME");
					for (int i = 0; i < writers.size(); i++)
					{
						//already encoded, relaying message to other clients
						if (i != index)
						{
							writers.get(i).println(firstPlayerName);
						}
					}
					needToSendName = false;
				}
				
				
				String instruction = reader.readLine();
				if (instruction != null)
				{
					System.out.println(clients);
					System.out.println(instruction);
					
					int splitIndex = instruction.indexOf(':');
					String type = instruction.substring(0,splitIndex);
					
					if (type.equals("READY") && index == 0 && clients.size()==1 )
					{
						firstPlayerName = instruction;
						needToSendName = true;
						System.out.println("HOLDING ONTO THE NAME");
						continue;
					}
					
					for (int i = 0; i < writers.size(); i++)
					{
						
						//already encoded, relaying message to other clients
						if (i != index)
						{
							writers.get(i).println(instruction);
						}
					}		
				}
			}
		}
		catch(Exception ex)
		{
			//disconnect
			System.out.println(clients);
			System.out.println("DISCONNECT!");
			
			for (int i = 0; i < writers.size(); i++)
			{
				//already encoded, relaying message to other clients
				if (i != index)
				{
					String outMessage = "DISCONNECT" + ":" + index;
					writers.get(i).println(outMessage);

				}
			}
			
			if (clients.size()==2)
			{
				clients.remove(index);
				writers.remove(index);
				return;
			}
			clients.remove(0);
			writers.remove(0);
			//end of thread
			return;
			
		}
	}
	
	/**
	 * will send a message to the client using a format.
	 * @param message message to send
	 * @param type type of message it is
	 */
	
	private void encodeAndSendMessage(String message, String type)
	{
		String outMessage = type + ":" + message;
		writer.println(outMessage);
	}
	
}
