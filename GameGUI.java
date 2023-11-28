import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
* The GameGUI class is responsible for all GUI and GUI to server methods.
* 
* @author Benjamin Jun-jie Glover 3035962764
* @version 1.0
* @since 28-11-2023
* 
* @param thisPlayerName stores the name of the player using this client
* @param otherPlayerName stores the name of the other player using the on the other client
* @param gameHasStarted used to determine whether the player has submitted their name
* @param otherPlayerReady used to determine whether the opponent has submitted their name and is ready to play
* @param isCrosses used to determine if the player will play crosses or circles
* @param replay determines if the player wants to play again
* @param isInTurn determines if it is currently the players turn
* @param frame  the window of the GUI
* @param frameWidth the width in pixels of the window
* @param frameHeight the height in pixels of the window
* @param c the gridBagConstriant of the GUI.
* @param gameBoardButtons stores all 9 of the buttons used for crosses and circles
* @param inputField
* @param submitButton
* @param options used for the buttons in the pop-up dialog
* @param info
* @param clientSocket stores the socket connecting this client to the server
* @param writer 
* @param streamReader
* @param reader
* 
*/

public class GameGUI {
		
	//instance variables
	String thisPlayerName;
	String otherPlayerName;
	boolean gameHasStarted;
	
	boolean otherPlayerReady;
	
	boolean isCrosses;
	boolean replay = false;
	
	boolean isInTurn = false;
	
	//frame
	JFrame frame;
	int frameWidth, frameHeight;
	GridBagConstraints c;
	
	//gamePanel
	ArrayList<GameBoardElement> gameBoardButtons;
	
	//inputBar
	JTextField inputField;
	JButton submitButton;
	
	//dialog panels
	String[] options = {"Yes", "No"}; 
	
	//infoBar
	JLabel info;
	
	//server communication
	Socket clientSocket;
	//io
	private PrintWriter writer;
	private InputStreamReader streamReader;
	private BufferedReader reader;
			
	
	/**
	 * This method resets the gridBagContraints c to its default values
	 * @param c the GridBagConstraints to be reset
	 */
	private void resetGridConstraints(GridBagConstraints c)
	{
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1; // default value
		c.gridheight = 1; // default value
		c.weightx = 0.0; // default value
		c.weighty = 0.0; // default value
		c.anchor = GridBagConstraints.CENTER; // default value
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 0, 0); // default value
		c.ipadx = 0; // default value
		c.ipady = 0; // default value
	}
	
	/**
	 * Constructor method
	 * @param sock the socket connecting to the server
	 */
	public GameGUI(Socket sock)
	{
		this.frameWidth = 500;
		this.frameHeight = 500;
		this.c = new GridBagConstraints();
		this.gameHasStarted = false;
		this.otherPlayerReady = false;
		
		this.isCrosses = false;
		
		this.gameBoardButtons = new ArrayList<GameBoardElement>();
				
		this.clientSocket = sock;
		
		try
		{
			writer = new PrintWriter(this.clientSocket.getOutputStream(), true);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		Runnable clientWorker = new ClientWorker(this.clientSocket);
		Thread clientThread = new Thread(clientWorker);
		clientThread.start();
		
		loadWidgets();
		
	}
	
	/**
	 * gameOverDialog handles the pop-up dialog event when a game has reached its end.
	 * will open a pop-up then either restart or end the game.
	 * @param message the message that will be used in the pop-up dialog box
	 * 
	 */
	private void gameOverDialog(String message)
	{
		int result = JOptionPane.showOptionDialog(
	               frame,
	               message, 
	               "GameOver",            
	               JOptionPane.YES_NO_OPTION,
	               JOptionPane.QUESTION_MESSAGE,
	               null,     //no custom icon
	               options,  //button titles
	               options[0] //default button
	            );
		//if anything other than yes is pressed, assume no pressed
		boolean replay = result == JOptionPane.YES_OPTION;
		
		if (replay == true)
		{
			//reset protocol
			System.out.println("RESTARTING");
			
			for (GameBoardElement tile: gameBoardButtons)
			{

				tile.reset();
			}
			
			isInTurn = false;
			info.setText("Waiting for your opponent to make a move");
			if (isCrosses)
			{
				info.setText("It is your turn, make a move");
				isInTurn = true;
			}
			//send other player the result, ie both want to restart then restart
			replay = false;
			otherPlayerReady=false;
			
			encodeAndSendMessage("TRUE", "RESTART");
			System.out.println("RESTARTING TRUE");
		}
		else
		{
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			encodeAndSendMessage("FALSE", "RESTART");
		}
			
	}
	
	/**
	 * loadWidgets loads all the widgets and creates all necessary objects.
	 */
	private void loadWidgets()
	{
		//frame
		frame = new JFrame();
		frame.setTitle("Tic Tac Toe");
		frame.setSize(frameWidth, frameHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		//menu bar
		JMenuBar menuBar = new JMenuBar();

		JMenu control = new JMenu("Control");
		JMenuItem exit = new JMenuItem("Exit");
		JMenu help = new JMenu("Help");
		JMenuItem instructions = new JMenuItem("Instruction");
		
		exit.addActionListener(new Exit());
		instructions.addActionListener(new Help());
		
		control.add(exit);
		help.add(instructions);
		menuBar.add(control);
		menuBar.add(help);
		
		frame.setJMenuBar(menuBar);
		
		//panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(mainPanel);
		
		//infoBar
		JPanel infoBar = new JPanel();
		infoBar.setBackground(Color.GREEN);
		info = new JLabel("Enter player name...");
		info.setBackground(Color.black);
		infoBar.add(info);
		
		//gamePanel 
		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(new GridLayout(3,3, 10, 10));
		gamePanel.setBackground(Color.BLACK);
		
		for (int i = 0; i<3; i++)
		{
			for (int j = 0; j<3; j++)
			{
				GameBoardElement button = new GameBoardElement(i,j);
				gameBoardButtons.add(button);
				c.gridx = i;
				c.gridy = j;
				button.getJButton().addActionListener(new GameButton(button));
				gamePanel.add(button.getJButton(), c);
				resetGridConstraints(c);
			}
		}
		
		
		//nameInputBar
		JPanel inputBar = new JPanel();
		inputBar.setBackground(Color.BLUE);
		
		inputField = new JTextField(20); 
		submitButton = new JButton("Submit");
		submitButton.addActionListener(new Submit());
		
		inputBar.add(inputField);
		inputBar.add(submitButton);
			
		
		//adding subPanels into the mainPanel
		resetGridConstraints(c);
		c.gridy = 0;
		mainPanel.add(infoBar,c);
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
 		mainPanel.add(gamePanel,c);
		resetGridConstraints(c);
		c.gridy = 2;
		mainPanel.add(inputBar,c);
		resetGridConstraints(c);
		
		//final render
		frame.setVisible(true);
	}
	
	
	/**
	 * this method will make string messages in a specific format so they can be easily read on the server/other client side.
	 * @param message the message to be sent
	 * @param type the category of the message
	 */
	void encodeAndSendMessage(String message, String type)
	{
		String outMessage = type + ":" + message;
		writer.println(outMessage);
	}
	
	/**
	 * inner class used to implement the Action Listener of the exit button in menu bar
	 * @author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * 
	 */
	//action listeners
	class Exit implements ActionListener
	{
		/**
		 * implemented method from Action listener, closes the GUI when pressed.
		 */
		public void actionPerformed(ActionEvent event) 
		{
			encodeAndSendMessage("DISCONNECT", "DISCONNECT");
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}

	/**
	 * inner class used to implement the Action Listener of the instruction button in menu bar
	 *@author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * 
	 */
	
	class Help implements ActionListener
	{
		/**
		 * implemented method from Action listener, activates a pop-up dialog box with information when pressed.
		 */
		public void actionPerformed(ActionEvent event) 
		{
			JOptionPane.showMessageDialog(frame, 
					"Criteria for a valid move:\r\n"
					+ "- The move is not occupied by any mark.\r\n"
					+ "- The move is made in the player’s turn.\r\n"
					+ "- The move is made within the 3 x 3 board.\r\n"
					+ "The game would continue and switch among the opposite player until it reaches either\r\n"
					+ "one of the following conditions:\r"
					+ "\n- Player 1 wins."
					+ "\n- Player 2 wins."
					+ "\n- Draw.");
		}
	}
	
	/**
	 * inner class used to implement the Action Listener of the submit button for submitting player name
	 *@author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * 
	 */
	class Submit implements ActionListener
	{
		/**
		 * implemented method from Action listener, deactivates the button and input field, and permanently set name when pressed.
		 */
		public void actionPerformed(ActionEvent event)
		{
			String name = inputField.getText();
			if (name.equals("") == false && gameHasStarted == false)
			{
				//disable the submit button
				submitButton.setForeground(Color.GRAY);
				submitButton.setEnabled(false);
				
				//disable the inputField
				inputField.setText(name);
				inputField.setForeground(Color.GRAY);
				inputField.setEditable(false);
				
				//update infoBar
				info.setText("WELCOME " + name);
				
				//update frameName
				
				//save the name and start game
				thisPlayerName = name;
				System.out.println("[program log <SUBMIT_BTN>] NAME DEACTIVATED");
				
				//game is started
				gameHasStarted = true;
				
				//send data over
				encodeAndSendMessage(name, "READY");
				
				
			}
			else
				System.out.println("[program log <SUBMIT_BTN>] NO NAME IN FIELD");
		}
	}
	
	/**
	 * inner class used to implement the Action Listener of the 9 tiles in the game board that player can turn into crosses/circles.
	 *@author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * 
	 */
	public class GameButton implements ActionListener
	{
		GameBoardElement gbe;
		
		/**
		 * the Constructor method for the GameButton
		 * @param gbe reference to the GameBoardElement that holds the JButton implementing this.
		 */
		public GameButton(GameBoardElement gbe)
		{
			this.gbe = gbe;
		}
		
		/**
		 * implemented method from Action listener, will change tile if it is pressed.
		 */
		public void actionPerformed(ActionEvent event)
		{
			System.out.println(gbe.getCoordinate() + " has been pressed");
			if (gbe.getHasBeenUsed() == false && otherPlayerReady && gameHasStarted && isInTurn)
			{
				isInTurn = false;
				gbe.setHasBeenUsed(true);
				gbe.renderImage();
				checkWin();
			
			}
		}
		
		/**
		 * checkWin checks if a win condition has been met on the board after a move
		 */
		private void checkWin()
		{
			int rowCount = 0;
			int colCount = 0;
			int drawCount = 0;
			
			int LDiagCount = 0;
			int RDiagCount = 0;
			
			for (GameBoardElement otherTile: gameBoardButtons)
			{
				if (otherTile.isSameRow(gbe.getRow()))
					rowCount +=1;
				if (otherTile.isSameCol(gbe.getCol()))
					colCount +=1;
				if (otherTile.isOnLDiag())
					LDiagCount+=1;
				if (otherTile.isOnRDiag())
					RDiagCount+=1;
				if (otherTile.getHasBeenUsed() == true)
					drawCount +=1;
			}
			
			if (rowCount == 3 || colCount == 3 || LDiagCount == 3 || RDiagCount == 3)
			{
				//change info bar
				info.setText("YOU HAVE WON!");
				encodeAndSendMessage(gbe.getCoordinate(), "WIN");
				gameOverDialog("Congratulations, you have won!\nDo you want to play again?");
				
			}
			else if (drawCount == 9)
			{
				//change info bar
				info.setText("IT IS A DRAW!");
				encodeAndSendMessage(gbe.getCoordinate(), "DRAW");
				gameOverDialog("GAMEOVER! it is a draw.\nDo you want to play again?");
			}
			else
			{
				info.setText("Valid Move, wait for your opponent!");
				encodeAndSendMessage(gbe.getCoordinate(), "TURN");
			}
		}
	}
	
	
	/**
	 * clientWorker is a inner class responsible for handling the continuous reading of server messages.
	 * will read messages and carry out actions based on the messages
	 * @author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * 
	 * @param clientSocket the socket connecting to the Server
	 */
	public class ClientWorker implements Runnable {
		//instance variables
		private Socket clientSocket;
		
		/**
		 * the Constructor method
		 * @param sock the socket connecting to the server.
		 */
		public ClientWorker(Socket sock)
		{
			this.clientSocket = sock;
			System.out.println("[program log <CLIENT>] CLIENT WORKER CREATED");
		}
		
		/**
		 * the implemented run method used by the thread.
		 * will continuously read for messages coming from the server and decipher them.
		 * then do actions to the GUI based on the content of the messages.
		 */
		public void run()
		{
			try
			{
				streamReader = new InputStreamReader(this.clientSocket.getInputStream());
				reader = new BufferedReader(streamReader);
				
				while (true)
				{
					if (clientSocket.isClosed())
					{
						JOptionPane.showMessageDialog(frame,"CONNECTION ERROR!");
						frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
						return;
					}
					String instruction = reader.readLine();
					if (instruction != null)
					{
						int splitIndex = instruction.indexOf(':');
						String type = instruction.substring(0,splitIndex);
						String message = instruction.substring(splitIndex+1);
						//decode the message and change the GUI
						System.out.println(type + "|" + message);
						
						//do all the GUI commands here
						switch(type)
						{
								
							//generic server messages
							case "PRINT":
							{
								System.out.println(message);
								break;
							}
							
							//when client connects to server
							//selects
							case "INIT":
							{
								if (message.equals("1"))
								{
									isCrosses = true;
									isInTurn = true;

								}
								
								break;
							}
							
							//when another player has entered their name
							case "READY":
							{
								otherPlayerName = message;
								otherPlayerReady = true;
								break;
							}
							
							//other player has made a move
							case "TURN":
							{
								System.out.println(otherPlayerName + " HAD MADE A MOVE ON " + message);
								
								for (GameBoardElement gbe: gameBoardButtons)
								{
									if (gbe.getCoordinate().equals(message))
									{
										gbe.opponentRenderImage();
									}
								}
								
								info.setText("Your opponent " + otherPlayerName + " has moved, now it is your turn");
								
								isInTurn = true;
								break;
							}
							
							case "WIN":
							{
								//OTHER OPPONENT WON
								System.out.println(otherPlayerName + " HAS WON");
								System.out.println(otherPlayerName + " HAD MADE A WINNING MOVE ON " + message);
								info.setText("Your opponent " + otherPlayerName + " has won. YOU HAVE LOST!");
								for (GameBoardElement gbe: gameBoardButtons)
								{
									if (gbe.getCoordinate().equals(message))
									{
										gbe.opponentRenderImage();
									}
								}
								isInTurn = false;
								gameOverDialog("Unfortunately you have lost! Do you want to play again?");
								
								break;
							}
							
							case "DRAW":
							{
								System.out.println("You have drawn");
								//NO SQUARES LEFT
								for (GameBoardElement gbe: gameBoardButtons)
								{
									if (gbe.getCoordinate().equals(message))
									{
										gbe.opponentRenderImage();
									}
								}
								isInTurn = false;
								gameOverDialog("GAMEOVER! it is a draw\nDo you want to play again?");
								break;
							}
							
							case "DISCONNECT":
							{
								System.out.println("DISCONNECT!");
								JOptionPane.showMessageDialog(frame,"The other player " + otherPlayerName + " has left!");
								frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
								break;
							}
							
							case "ACCEPTED":
							{
								if (message.equals("FALSE"))
								{
									System.out.println("DISCONNECT!");
									clientSocket.close();
									JOptionPane.showMessageDialog(frame,"TOO MANY PLAYERS! CANNOT CONNECT");
									frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
								}
								break;
							}
							
							case "RESTART":
							{
								if (message.equals("FALSE"))
								{
									JOptionPane.showMessageDialog(frame,"The other player " + otherPlayerName + " has left!");
									frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
								}
								else
								{
									otherPlayerReady = true;
								}
								break;
							}
							
							
					
						}
					}
				}			
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
	
	
	/**
	 * GameBoardElement is a inner class used to do methods and hold instance variables for each tile in the game board.
	 *@author Benjamin Jun-jie Glover 3035962764
	 * @version 1.0
	 * @since 28-11-2023'
	 * @param hasBeenUsed boolean to determine if the tile can be clicked by player
	 * @param emptyImagePath the image path of a unused tile
	 * @param value 
	 * @param row
	 * @param col
	 * @param imageIcon
	 * @param button the JButton instance variable of this class
	 * 
	 */
	class GameBoardElement
	{
		private boolean hasBeenUsed = false;

		private String emptyImagePath = "src/Images/empty.gif";
		private String value = "";
		private int row, col;
		
		private ImageIcon imageIcon = null;
		private JButton button = null;
		
		/**
		 * Constructor method for GameBoardElement
		 * @param row the row position of the tile
		 * @param col the column position of the tile
		 */
		public GameBoardElement(int row, int col)
		{
			this.button = new JButton();
			this.row = row;
			this.col = col;
			this.hasBeenUsed = false;
			renderImage();
		}
		
		/**
		 * resets the tile to default values
		 */
		public void reset()
		{
			this.value = "";
			this.hasBeenUsed = false;
			this.imageIcon = null;
			renderImage();
		}
		
		/**
		 * renders the tile with its current values
		 */
		public void renderImage()
		{
			
			if (hasBeenUsed)
			{
				imageIcon = new ImageIcon("src/Images/" + getThisPlayerType() + ".gif");
				this.value = getThisPlayerType();
			}
			else
			{
				imageIcon = new ImageIcon(emptyImagePath);
				this.value = "";
			}
			
			this.button.setIcon(imageIcon);
		}
		
		/**
		 * renders the tile after an opponent has taken it
		 */
		public void opponentRenderImage()
		{
			hasBeenUsed = true;
			imageIcon = new ImageIcon("src/Images/" + getOpponentType() + ".gif");	
			this.button.setIcon(imageIcon);
			this.value = getOpponentType();
		}
			
		/**
		 * returns whether the player is crosses or circles
		 * @return String the player team
		 */
		public String getThisPlayerType()
		{
			if (isCrosses == true)
				return "cross";
			else
				return "circle";
		}
		
		/**
		 * returns whether the opponent is crosses or circles
		 * @return String the opponent team
		 */
		public String getOpponentType()
		{
			if (isCrosses == true)
				return "circle";
			else
				return "cross";
		}
		
		/**
		 * determines if 2 tiles are in the same row
		 * @return boolean true if both tiles are in same row
		 */
		public boolean isSameRow(int rowToCompareWith)
		{		
			if (this.row == rowToCompareWith)
			{
				if(this.value.equals(getThisPlayerType()))
				{
					return true;
				}
			}
			return false;
		}
		
		/**
		 * determines if 2 tiles are in the same column
		 * @return boolean true if both tiles are in same column
		 */
		public boolean isSameCol(int colToCompareWith)
		{		
			if (this.col == colToCompareWith)
			{
				if(this.value.equals(getThisPlayerType()))
				{
					return true;
				}
			}
			return false;
		}
		
		/**
		 * determines if a tile is on the left diagonal
		 * @return boolean true if a tile are is on the left diagonal
		 */
		public boolean isOnLDiag()
		{		
			if (this.col == this.row)
			{
				if(this.value.equals(getThisPlayerType()))
					return true;
			}
			return false;
		}
		
		/**
		 * determines if a tile are is on the right diagonal
		 * @return boolean true if both tile is on the right diagonal
		 */
		public boolean isOnRDiag()
		{		
			if (this.col == 2 - this.row)
			{
				if(this.value.equals(getThisPlayerType()))
					return true;
			}
			return false;
		}
		
		/**
		 * gets the JButton instance variable
		 * @return JButton the JButton instance variable
		 */
		public JButton getJButton()
		{
			return this.button;
		}
		
		/**
		 * gets the JButton instance variable
		 * @return boolean the JButton instance variable
		 */
		public boolean getHasBeenUsed()
		{
			return this.hasBeenUsed;
		}
		
		/**
		 * gets the String of the coordinates of the tile
		 * @return String
		 */
		public String getCoordinate()
		{
			return row + "," + col; 
		}
		
		/**
		 * gets row
		 * @return Integer
		 */
		public int getRow()
		{
			return this.row;
		}
		
		
		/**
		 * gets column
		 * @return Integer
		 */
		public int getCol()
		{
			return this.col;
		}
		
		/**
		 * sets the hasBeenUSed instance variable
		 * @param set the value to set hasBeenUsed to.
		 */
		public void setHasBeenUsed(boolean set)
		{
			this.hasBeenUsed = set;;
		}

	}
	
	
	
}
