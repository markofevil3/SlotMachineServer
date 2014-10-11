package com.yna.game.slotmachine.models;

public class GameType {

	public static final String SLOT_TYPE_FRUITS = "slot_fruit";
	public static final String SLOT_TYPE_HALLOWEEN = "slot_halloween";
	
	private static final String FRUIT_LOBBY_ROOM = "fruitLobby";
	private static final String FRUIT_ROOM_GROUP = "fruitRooms";
	private static final String FRUIT_ROOM_NAME = "fRoom";
	
	private static final String HALLOWEEN_LOBBY_ROOM = "halloweenLobby";
	private static final String HALLOWEEN_ROOM_GROUP = "halloweenRooms";
	private static final String HALLOWEEN_ROOM_NAME = "hRoom";
	
	public static String GetLoobyRoom(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_LOBBY_ROOM;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_LOBBY_ROOM;
		}
		return null;
	}
	
	public static String GetRoomGroup(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_ROOM_GROUP;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_ROOM_GROUP;
		}
		return null;
	}
	
	public static String GetRoomCode(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_ROOM_GROUP;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_ROOM_GROUP;
		}
		return null;
	}
}
