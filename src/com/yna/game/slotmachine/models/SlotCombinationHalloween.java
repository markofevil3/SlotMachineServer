package com.yna.game.slotmachine.models;

import java.util.Random;

import org.json.JSONObject;

import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

public class SlotCombinationHalloween {
	
  public static Random random;
  public static int[] ITEM_RATES = new int[] {3, 8, 12, 11, 11, 10, 9, 8, 8, 6, 14};
  public static int[] SPECIAL_ITEM_RATES = new int[] {3, 0, 0, 10, 14, 18, 16, 16, 14, 9, 0};  
  public static int[][] PAYOUTS = new int[][] {
    // item 0 - ignore
    { 0, 0, 0, 0, 0 },
    // item 1
    { 0, 10, 30, 100, 850 },
    // item 2
    { 0, 0, 150, 300, 1500 },
    // item 3
    { 0, 0, 200, 400, 2500 },
    // item 4
    { 0, 0, 250, 500, 4000 },
    // item 5
    { 0, 0, 300, 700, 5500 },
    // item 6
    { 0, 0, 350, 800, 6500 },
    // item 7
    { 0, 0, 450, 1000, 8000 },
    // item 8
    { 0, 0, 750, 1750, 12500 },
    // item 9
    { 0, 0, 1000, 2000, 17500 },
    // special item
    { 0, 0, 5, 7, 15 }
  };
  
	public static void Init() {
  	random = new Random();
	}
	
	public static JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
		return null;
	}
	
	public static JSONObject GetGameVariable(User player, Room room) {
		return null;
	}
}
