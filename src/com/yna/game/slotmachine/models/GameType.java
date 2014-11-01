package com.yna.game.slotmachine.models;

import java.util.Random;

import com.yna.game.common.Util;

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
			return FRUIT_ROOM_NAME;
		case SLOT_TYPE_HALLOWEEN:
			return HALLOWEEN_ROOM_NAME;
		}
		return null;
	}
	
	public static Random GetRandomMethod(String gameType) {
		switch (gameType) {
		case SLOT_TYPE_FRUITS:
			return SlotCombinationFruit.random;
		case SLOT_TYPE_HALLOWEEN:
			return SlotCombinationHalloween.random;
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
		}
		Util.log("######GameType - GetPayout null : " + gameType);
		return null;
	}
}
