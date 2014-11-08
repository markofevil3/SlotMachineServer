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
	
	private static final String FRUIT_LOBBY_ROOM = "fruitLobby";
	private static final String FRUIT_ROOM_GROUP = "fruitRooms";
	private static final String FRUIT_ROOM_NAME = "fRoom";
	
	private static final String HALLOWEEN_LOBBY_ROOM = "halloweenLobby";
	private static final String HALLOWEEN_ROOM_GROUP = "halloweenRooms";
	private static final String HALLOWEEN_ROOM_NAME = "hRoom";
	
	private static final String DRAGON_LOBBY_ROOM = "dragonLobby";
	private static final String DRAGON_ROOM_GROUP = "dragonRooms";
	private static final String DRAGON_ROOM_NAME = "dRoom";

	
	public static String GetLoobyRoom(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return FRUIT_LOBBY_ROOM;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_LOBBY_ROOM;
		case SLOT_TYPE_DRAGON:
			return DRAGON_LOBBY_ROOM;
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
		}
		return null;
	}
	
	public static Random GetRandomMethod(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.random;
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.random;
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.random;
		}
		Util.log("######GameType - GetRandomMethod null : " + gameType);
		return new Random();
	}

	public static int[] GetSpecialItemRate(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.SPECIAL_ITEM_RATES;
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.SPECIAL_ITEM_RATES;
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.SPECIAL_ITEM_RATES;
		}
		Util.log("######GameType - GetSpecialItemRate null : " + gameType);
		return null;
	}
	
	public static int[] GetItemRate(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.ITEM_RATES;
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.ITEM_RATES;
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.ITEM_RATES;
		}
		Util.log("######GameType - GetItemRate null : " + gameType);
		return null;
	}
	
	public static int[][] GetPayout(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.PAYOUTS;
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.PAYOUTS;
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.PAYOUTS;
		}
		Util.log("######GameType - GetPayout null : " + gameType);
		return null;
	}

	public static JSONObject SetGameVariable(String gameType, User player, Room room, SFSApi sfsApi) {
		switch(gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.SetGameVariable(player, room, sfsApi);
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.SetGameVariable(player, room, sfsApi);
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.SetGameVariable(player, room, sfsApi);
		}
		return null;
	}
	
	public static JSONObject GetGameVariable(String gameType, User player, Room room) {
		switch(gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.GetGameVariable(player, room);
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.GetGameVariable(player, room);
		case SLOT_TYPE_DRAGON:
			return SlotCombinationDragon.GetGameVariable(player, room);
		}
		return null;
	}
}
