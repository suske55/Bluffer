package Reactor.protocol;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
* This class represents a room that players can join and play games. Keeps a map for all the players in the room.
* Keeps a map for all the games in the room.
*/

public class Room {
	
	private String name;
	private int playersInRoom;
	private int playersStarted;
	private HashMap<String , TBGProtocol> inRoom;
	private HashMap<String,Object> Game;
	private boolean gameActive;

	public Room(String room) {
		name = room;
		inRoom = new HashMap<>();
		Game = new HashMap<>();
		Game.put("BLUFFER", new Bluffer());
		gameActive = false;
		playersInRoom = 0;
		playersStarted = 0;
	}
	public Object getGame(String game){
		return Game.get(game);
	}
	public boolean isGameActive(){
		return gameActive;
	}
	public int getNumOfPlayers(){
		return playersStarted;
	}
	
	public void removeFromRoom(String name){
		inRoom.remove(name);
		playersInRoom--;
	}
	public void joinRoom(String name , TBGProtocol proto){
		playersInRoom++;
		inRoom.put(name, proto);
	}
	public ArrayList listGames(){
		ArrayList<String> ans = new ArrayList();
		ans.addAll(Game.keySet());
		return ans;
		
	}
	public boolean startGame(String game){
		if (Game.containsKey(game)){
			playersStarted++;
			if (playersStarted == playersInRoom){
				Scanner sc = new Scanner(System.in);
				try {
					((Bluffer) Game.get(game)).parseJson(sc.nextLine());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				gameActive = true;
				for (String s : inRoom.keySet()){
					inRoom.get(s).drawFirstQuestion(game);
					
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public void endGame(){
		playersStarted = 0;
		Game.put("BLUFFER", new Bluffer());
		gameActive = false;
		
	}
	
	public void sendToAll(String msg , TBGProtocol proto) {
		for (TBGProtocol p : inRoom.values()){
			if(!p.equals(proto)){
				try {
					p.getCallback().sendMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
