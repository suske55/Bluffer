package Reactor.protocol;

import java.io.IOException;
import java.util.ArrayList;

import Reactor.tokenizer.FixedSeparatorMessageTokenizer;
import Reactor.tokenizer.MessageTokenizer;
import Reactor.tokenizer.StringMessage;


/**
 * 
 * This class implements the ServerProtocol interface and represents a player's game protocol.
 * It acts like the game's rules and determines how to respond to each command that was received from the player.
 * It also keeps track the player's points in a game.
 * It is individual for each client.
 * 
 */
public class TBGProtocol implements ServerProtocol<String>{

	private String name;
	private int points = 0;
	private int totalPoints = 0;
	private String currentQuestion;
	private String realAnswer;
	private int myAnswer;
	private int maxQuestions = 3;
	private int questionNum = 0;
	private ProtocolCallback callback;
	private Room myRoom;
	private boolean state = false;
	private boolean shouldClose;


	@Override
	public void processMessage(String msg, ProtocolCallback callback) {
		this.callback = callback;
		String command; 
		if (msg.contains(" ")){
			command = msg.substring(0, msg.indexOf(" "));
			msg = msg.substring(msg.indexOf(" ")+1);
			msg.trim();
		}
		else command = msg;
		//if NICK
		if (command.equals("NICK")){
			if (name == null){
				if (msg.equals("")){
					try {
						callback.sendMessage("SYSMSG NICK " + msg + " REJECTED , you must enter a name");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					if(ServerData.getInstance().addPlayer(msg)){
						name = msg;
						try {
							callback.sendMessage("SYSMSG NICK " + msg + " ACCEPTED");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else{
						try {
							callback.sendMessage("SYSMSG NICK " + msg + " REJECTED , username already exists");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			else{
				try {
					callback.sendMessage("SYSMSG NICK " + msg + " REJECTED , names cannot be changed");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if (command.equals("JOIN")){
			if (msg.equals("")){
				try {
					callback.sendMessage("SYSMSG JOIN " + msg + " REJECTED , you must enter a room name");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				if (ServerData.getInstance().joinRoom(name, msg, this)){
					myRoom = ServerData.getInstance().getRooms().get(ServerData.getInstance().getRoomBYplayer().get(name));
					try {
						callback.sendMessage("SYSMSG JOIN " + msg + " ACCEPTED");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					if (myRoom.isGameActive()){
						try {
							callback.sendMessage("SYSMSG JOIN " + msg + " REJECTED , cant leave in a middle of a game");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						callback.sendMessage("SYSMSG JOIN " + msg + " REJECTED , game already in progress");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (command.equals("LISTGAMES")){
			if (ServerData.getInstance().getRoomBYplayer().get(name) == null){
				try {
					callback.sendMessage("SYSMSG LISTGAMES REJECTED , you are not in a room!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ArrayList<String> tmp = myRoom.listGames();
			int i = 1;
			for (String game: tmp){
				String ans = i + ". " + game;
				try {
					callback.sendMessage(ans);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if (command.equals("STARTGAME")){
			if (myRoom.startGame(msg)){
			}
			else{
				try {
					callback.sendMessage("SYSMSG STARTGAME REJECTED , no such game");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if (command.equals("TXTRESP")){
			if (msg.equals("")){
				try {
					callback.sendMessage("SYSMSG TXTRESP REJECTED , you must give an answer");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				if (state == false){
					msg = msg.toLowerCase();
					((Bluffer)myRoom.getGame("BLUFFER")).storeAnswer(msg, this, myRoom.getNumOfPlayers(), realAnswer);
					try {
						callback.sendMessage("SYSMSG TXTRESP ACCEPTED");
					} catch (IOException e) {
						e.printStackTrace();
					}
					while(!((Bluffer)myRoom.getGame("BLUFFER")).allAnswerd()){

					}
					String choices = ((Bluffer)myRoom.getGame("BLUFFER")).getChoices();
					state = true;
					try {
						callback.sendMessage("ASKCHOICES " + choices);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						callback.sendMessage("SYSMSG TXTRESP REJECTED , expecting SELECTRESP");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		else if (command.equals("SELECTRESP")){
			if (state == true){
				int ans = Integer.parseInt(msg);
				if ((Integer)ans==null){
					try {
						callback.sendMessage("SYSMSG SELECTRESP REJECTED , choose the number of your answer");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					if(ans>=0 && ans<=myRoom.getNumOfPlayers()){
						state = false;
						try {
							callback.sendMessage("SYSMSG SELECTRESP ACCEPTED");
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							callback.sendMessage("GAMEMSG The correct answer is: "+ realAnswer);
						} catch (IOException e) {
							e.printStackTrace();
						}
						myAnswer=ans;
						if(((Bluffer)myRoom.getGame("BLUFFER")).answerSummary(myAnswer,this, realAnswer)){
							while(!((Bluffer)myRoom.getGame("BLUFFER")).allAnswerd()){

							}
							try {
								callback.sendMessage("GAMEMSG correct! +" + points + "pts");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else{
							while(!((Bluffer)myRoom.getGame("BLUFFER")).allAnswerd()){

							}
							try {
								callback.sendMessage("GAMEMSG Wrong! +" + points+"pts");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						totalPoints = totalPoints + points;
						points = 0;

					}
					else{
						try {
							callback.sendMessage("SYSMSG SELECTRESP REJECTED , choose number between 0 and " + (myRoom.getNumOfPlayers()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				questionNum++;
				if (questionNum == 3){
					questionNum = 0;
					try {
						callback.sendMessage(((Bluffer)myRoom.getGame("BLUFFER")).pointsSummary());
					} catch (IOException e) {
						e.printStackTrace();
					}
					totalPoints = 0;
					myRoom.endGame();
				}
				else{
					((Bluffer) myRoom.getGame("BLUFFER")).reset();
					currentQuestion = ((Bluffer) myRoom.getGame("BLUFFER")).getQuestion(questionNum);
					realAnswer = ((Bluffer) myRoom.getGame("BLUFFER")).getAnswer(currentQuestion);
					try {
						callback.sendMessage("ASKTXT " + currentQuestion);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else{
				try {
					callback.sendMessage("SYSMSG SELECTRESP REJECTED , no choice available");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(command.equals("MSG")){
			try {
				callback.sendMessage("SYSMSG MSG ACCEPTED");
			} catch (IOException e) {
				e.printStackTrace();
			}
			myRoom.sendToAll( "USRMSG " + name + ": " + msg , this);
		}
		else if(command.equals("QUIT")){
			if(myRoom != null){
				if (myRoom.isGameActive()){
					try {
						callback.sendMessage("SYSMSG QUIT REJECTED , cant quit in a middle of a game");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						callback.sendMessage("SYSMSG QUIT ACCEPTED");
					} catch (IOException e) {
						e.printStackTrace();
					}
					ServerData.getInstance().getPlayers().remove(name);
					ServerData.getInstance().getRoomBYplayer().remove(name);
					isEnd(command);
				}
			}
			else{
				try {
					callback.sendMessage("SYSMSG QUIT ACCEPTED");
				} catch (IOException e) {
					e.printStackTrace();
				}
				ServerData.getInstance().getPlayers().remove(name);
				ServerData.getInstance().getRoomBYplayer().remove(name);
				isEnd(command);

			}
		}
		else {
			try {
				callback.sendMessage("SYSMSG " + command+ " UNIDENTIFIED");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawFirstQuestion(String msg){
		currentQuestion = ((Bluffer) myRoom.getGame(msg)).getQuestion(questionNum);
		realAnswer = ((Bluffer) myRoom.getGame(msg)).getAnswer(currentQuestion);
		try {
			callback.sendMessage("SYSMSG STARTGAME ACCEPTED , waiting for other players...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			callback.sendMessage("ASKTXT " + currentQuestion);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isEnd(String msg) {
		if(msg.equals("QUIT")){
			connectionTerminated();
			return true;
		}
		else return false;
	}

	public int getPoints(){
		return totalPoints;
	}
	public void setPoints(int i){
		points = points+i;
	}
	public int getMyAnswer(){
		return myAnswer;
	}
	public String getMyName(){
		return name;
	}
	public ProtocolCallback getCallback(){
		return callback;
	}


	public boolean shouldClose() {
		return shouldClose;
	}


	public void connectionTerminated() {
		shouldClose = true;

	}

}
