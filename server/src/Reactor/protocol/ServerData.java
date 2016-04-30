package Reactor.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
* This class is singleton holding all the general data of the server including a map of the total player connected,
* a map of all the rooms and a map for sorting players by there current room.
*
*/
public class ServerData {

	private Set<String> players;
	private Map<String,String> roomBYplayer;
	private Map<String,Room> rooms;
	private String JsonPath;

	private ServerData(){
		players = new HashSet<>();
		roomBYplayer = new HashMap<>();
		rooms = new HashMap<>();
	}

	private static class ServerDataHolder{
		private static ServerData instance = new ServerData();
	}

	public static ServerData getInstance(){
		return ServerDataHolder.instance;
	}

	public Map<String, String> getRoomBYplayer() {
		return roomBYplayer;
	}

	public Map<String, Room> getRooms() {
		return rooms;
	}

	public Set<String> getPlayers(){
		return players;
	}

	public boolean addPlayer(String name){
		if (players.contains(name)){
			return false;
		}
		else {
			players.add(name);
			return true;
		}
	}
	public boolean joinRoom(String name , String room, TBGProtocol proto){
		if (roomBYplayer.get(name) != null){
			if(rooms.get(roomBYplayer.get(name)).isGameActive()){
				return false;
			}
		}
		if (!rooms.containsKey(room)){
			Room tmp = new Room(room);
			rooms.putIfAbsent(room, tmp );
		}
		else if (rooms.get(room).isGameActive()){
			return false;
		}

		if (roomBYplayer.get(name) == null){
			roomBYplayer.put(name, room);
			rooms.get(room).joinRoom(name, proto);
		}
		else {
			rooms.get(roomBYplayer.get(name)).removeFromRoom(name);
			roomBYplayer.put(name, room);
			rooms.get(room).joinRoom(name, proto);
		}
		return true;


	}

}
