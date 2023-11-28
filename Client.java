import java.net.*;

/**
* The Server class is responsible for accepting incoming clients and allocating threads to them.
* 
* @author Benjamin Jun-jie Glover 3035962764
* @version 1.0
* @since 28-11-2023
* 
* @param clientSocket the socket to the server
* @param serverIP
* @param serverPort
* @param game a reference variable to the GameGUI object.
* 
*/
public class Client {
	
	Socket clientSocket;
	String serverIP;
	int serverPort;
	GameGUI game;
	
	/**
	 * main method
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		Client client = new Client();
	}
	
	/**
	 * Constructor method
	 */
	public Client()
	{
		
		this.serverIP = "127.0.0.1";
		this.serverPort = 6000;
		connect();
		System.out.println("CLIENT" + this.clientSocket);
		
		
		if (!clientSocket.isClosed())
		{
			game = new GameGUI(this.clientSocket);		
		}
		
	}
	
	/**
	 * attempts to connect to the server
	 */
	private void connect()
	{
		try
		{
			System.out.println("[program log <CLIENT>] TRYING TO CONNECT...");
			this.clientSocket = new Socket(serverIP, serverPort);
			System.out.println("[program log <CLIENT>] SUCCESSFUL CONNECT");
		}
		catch(Exception ex)
		{
			clientSocket = null;
			ex.printStackTrace();
			System.out.println("[program log <CLIENT>] FAILED CONNECT");
		}
	}
}
