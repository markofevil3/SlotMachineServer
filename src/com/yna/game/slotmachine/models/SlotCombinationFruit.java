package com.yna.game.slotmachine.models;

import java.util.Random;

import org.json.JSONObject;

import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

public class SlotCombinationFruit {
	
  public static Random random;
  public static int[] ITEM_RATES = new int[] {3, 8, 12, 11, 11, 10, 9, 8, 8, 6, 14};
  public static int[] SPECIAL_ITEM_RATES = new int[] {3, 0, 0, 10, 14, 18, 16, 16, 14, 9, 0};  
  public static int[][] PAYOUTS = new int[][] {
    // item 0 - ignore
    { 0, 0, 0, 0, 0 },
    // item 1
    { 0, 1, 3, 10, 85 },
    // item 2
    { 0, 0, 15, 30, 150 },
    // item 3
    { 0, 0, 20, 40, 250 },
    // item 4
    { 0, 0, 25, 50, 400 },
    // item 5
    { 0, 0, 30, 70, 550 },
    // item 6
    { 0, 0, 35, 80, 650 },
    // item 7
    { 0, 0, 45, 100, 800 },
    // item 8
    { 0, 0, 75, 175, 1250 },
    // item 9
    { 0, 0, 100, 200, 1750 },
    // special item
    { 0, 0, 5, 7, 15 }
  };
  
	public static void Init() {
  	random = new Random();
	}
	
	public static JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
		return new JSONObject();
	}
	
	public static JSONObject UpdateGameVariable(User player, Room room, SFSApi sfsApi, JSONObject jsonData) {
		return new JSONObject();
	}
	
	public static JSONObject GetGameVariable(User player, Room room) {
		return new JSONObject();
	}
}
