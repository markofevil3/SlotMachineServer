package com.yna.game.slotmachine.models;

import java.util.Random;

import org.json.JSONObject;

import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.yna.game.common.Util;

public class GameType {

	public static final String SLOT_TYPE_FRUITS = "slot_fruit";
	public static final String SLOT_TYPE_HALLOWEEN = "slot_halloween";
	public static final String SLOT_TYPE_DRAGON = "slot_dragon";
	public static final String SLOT_TYPE_PIRATE= "slot_pirate";
	
	private static final String FRUIT_LOBBY_ROOM = "fruitLobby";
	private static final String FRUIT_ROOM_GROUP = "fruitRooms";
	private static final String FRUIT_ROOM_NAME = "fRoom";
	
	private static final String HALLOWEEN_LOBBY_ROOM = "halloweenLobby";
	private static final String HALLOWEEN_ROOM_GROUP = "halloweenRooms";
	private static final String HALLOWEEN_ROOM_NAME = "hRoom";
	
	private static final String DRAGON_LOBBY_ROOM = "dragonLobby";
	private static final String DRAGON_ROOM_GROUP = "dragonRooms";
	private static final String DRAGON_ROOM_NAME = "dRoom";

	private static final String PIRATE_LOBBY_ROOM = "pirateLobby";
	private static final String PIRATE_ROOM_GROUP = "pirateRooms";
	private static final String PIRATE_ROOM_NAME = "pRoom";
	
	public static SlotCombinationPirate slotPirateInstance;
	public static SlotCombinationDragon slotDragonInstance;
	
	public static String GetLoobyRoom(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_LOBBY_ROOM;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_LOBBY_ROOM;
		case SLOT_TYPE_DRAGON:
			return DRAGON_LOBBY_ROOM;
		case SLOT_TYPE_PIRATE:
			return PIRATE_LOBBY_ROOM;
		}
		return null;
	}
	
	public static String GetRoomGroup(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_ROOM_GROUP;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_ROOM_GROUP;
		case SLOT_TYPE_DRAGON:
			return DRAGON_ROOM_GROUP;
		case SLOT_TYPE_PIRATE:
			return PIRATE_ROOM_GROUP;
		}
		return null;
	}
	
	public static String GetRoomCode(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_ROOM_NAME;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_ROOM_NAME;
		case SLOT_TYPE_DRAGON:
			return DRAGON_ROOM_NAME;
		case SLOT_TYPE_PIRATE:
			return PIRATE_ROOM_NAME;
		}
		return null;
	}
	
  public static int[] GenerateRandomItems(boolean isFreeSpin, String gameType) {
		switch (gameType) {
		case SLOT_TYPE_DRAGON:
			return slotDragonInstance.GenerateRandomItems(isFreeSpin, gameType);
		case SLOT_TYPE_PIRATE:
			return slotPirateInstance.GenerateRandomItems(isFreeSpin, gameType);
		}
		Util.log("######GameType - GenerateRandomItems null : " + gameType);
		return null;
  }
	
  public static JSONObject CalculateCombination(int[] reelData, int numLines, int betPerLine, String gameType, JSONObject out) {
		switch (gameType) {
		case SLOT_TYPE_DRAGON:
			return slotDragonInstance.CalculateCombination(reelData, numLines, betPerLine, gameType, out);
		case SLOT_TYPE_PIRATE:
			return slotPirateInstance.CalculateCombination(reelData, numLines, betPerLine, gameType, out);
		}
		Util.log("######GameType - CalculateCombination null : " + gameType);
		return null;
	}
	
//	public static Random GetRandomMethod(String gameType) {
//		switch (gameType) {
//		case SLOT_TYPE_FRUITS:
//			return SlotCombinationFruit.random;
//		case SLOT_TYPE_HALLOWEEN:
//			return SlotCombinationHalloween.random;
//		case SLOT_TYPE_DRAGON:
//			return SlotCombinationDragon.random;
//		case SLOT_TYPE_PIRATE:
//			return SlotCombinationPirate.random;
//		}
//		Util.log("######GameType - GetRandomMethod null : " + gameType);
//		return new Random();
//	}

//	public static int[] GetSpecialItemRate(String gameType) {
//		switch (gameType) {
//		case SLOT_TYPE_FRUITS:
//			return SlotCombinationFruit.SPECIAL_ITEM_RATES;
//		case SLOT_TYPE_HALLOWEEN:
//			return SlotCombinationHalloween.SPECIAL_ITEM_RATES;
//		case SLOT_TYPE_DRAGON:
//			return SlotCombinationDragon.SPECIAL_ITEM_RATES;
//		case SLOT_TYPE_PIRATE:
//			return SlotCombinationPirate.SPECIAL_ITEM_RATES;
//		}
//		Util.log("######GameType - GetSpecialItemRate null : " + gameType);
//		return null;
//	}
	
//	public static int[] GetItemRate(String gameType) {
//		switch (gameType) {
//		case SLOT_TYPE_FRUITS:
//			return SlotCombinationFruit.ITEM_RATES;
//		case SLOT_TYPE_HALLOWEEN:
//			return SlotCombinationHalloween.ITEM_RATES;
//		case SLOT_TYPE_DRAGON:
//			return SlotCombinationDragon.ITEM_RATES;
//		case SLOT_TYPE_PIRATE:
//			return SlotCombinationPirate.ITEM_RATES;
//		}
//		Util.log("######GameType - GetItemRate null : " + gameType);
//		return null;
//	}
	
//	public static int[][] GetPayout(String gameType) {
//		switch (gameType) {
//		case SLOT_TYPE_FRUITS:
//			return SlotCombinationFruit.PAYOUTS;
//		case SLOT_TYPE_HALLOWEEN:
//			return SlotCombinationHalloween.PAYOUTS;
//		case SLOT_TYPE_DRAGON:
//			return SlotCombinationDragon.PAYOUTS;
//		case SLOT_TYPE_PIRATE:
//			return SlotCombinationPirate.PAYOUTS;
//		}
//		Util.log("######GameType - GetPayout null : " + gameType);
//		return null;
//	}

	public static JSONObject SetGameVariable(String gameType, User player, Room room, SFSApi sfsApi) {
		switch(gameType) {
		case SLOT_TYPE_DRAGON:
			return slotDragonInstance.SetGameVariable(player, room, sfsApi);
		case SLOT_TYPE_PIRATE:
			return slotPirateInstance.SetGameVariable(player, room, sfsApi);
		}
		return new JSONObject();
	}
	
	public static JSONObject UpdateGameVariable(String gameType, User player, Room room, SFSApi sfsApi, JSONObject jsonData, int totalWin) {
		switch(gameType) {
		case SLOT_TYPE_DRAGON:
			return slotDragonInstance.UpdateGameVariable(player, room, sfsApi, jsonData, totalWin);
		case SLOT_TYPE_PIRATE:
			return slotPirateInstance.UpdateGameVariable(player, room, sfsApi, jsonData, totalWin);
		}
		return jsonData;
	}
	
	public static JSONObject GetGameVariable(String gameType, User player, Room room) {
		switch(gameType) {
		case SLOT_TYPE_DRAGON:
			return slotDragonInstance.GetGameVariable(player, room);
		case SLOT_TYPE_PIRATE:
			return slotPirateInstance.GetGameVariable(player, room);
		}
		return new JSONObject();
	}
}
