import java.io.IOException;
import java.net.*;
import java.util.Map;

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

	GameGUI game;
	Map<String, String> env;

	Socket clientSocket;
	String serverIP;
	int serverPort;
	
	
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
		// get server configuration
		try{
			this.env = EnvLoader.loadEnv(".env");
			System.out.println("[CLIENT] .env found");
			this.serverIP = env.getOrDefault("SERVER_IP", "127.0.0.1");
			this.serverPort = Integer.parseInt(env.getOrDefault("SERVER_PORT", "6000"));
		} catch (IOException e) {
			System.out.println("[CLIENT] No .env found, falling back to localhost");
			this.serverIP = "127.0.0.1";
			this.serverPort = 6000;
		}

		if (!connect()) {
			System.exit(-1);
		}
		
		if (!clientSocket.isClosed()){
			this.game = new GameGUI(this.clientSocket);		
		}
		
	}
	
	/**
	 * attempts to connect to the server
	 */
	private boolean connect()
	{
		try{
			System.out.println("[CLIENT] TRYING TO CONNECT TO " + this.serverIP + ":" + this.serverPort + "...");
			this.clientSocket = new Socket(this.serverIP, this.serverPort);
			System.out.println("[CLIENT] SUCCESSFULLY CONNECTED");
			return true;
		}catch(Exception ex)
		{
			this.clientSocket = null;
			ex.printStackTrace();
			System.out.println("[CLIENT] FAILED TO CONNECT. Closing...");
			return false;
		}
	}
}
