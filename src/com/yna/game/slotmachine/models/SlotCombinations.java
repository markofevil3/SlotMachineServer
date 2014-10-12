package com.yna.game.slotmachine.models;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yna.game.common.Util;

public class SlotCombinations {
	
	public class SlotItem {
		public static final int WILD = 0;
		public static final int APPLE = 1;
		public static final int STRAWBERRY = 2;
		public static final int RADISH = 3;
		public static final int BROCCOLI = 4;
		public static final int EGGPLANT = 5;
		public static final int BELL_PEPPER = 6;
		public static final int CHILI_PEPPER = 7;
		public static final int MUSHROOM = 8;
		public static final int FRUIT_BASKET = 9;
		public static final int TOTAL = 10;
	}
	
  public static int MAX_LINE = 9;
  public static int NUM_REELS = 5;
  public static int MAX_DISPLAY_ITEMS = 15;
  
  private static int[] ITEM_RATES = new int[] {4, 20, 12, 11, 10, 10, 10, 9, 8, 6};
  
  private static float randomValue;
  private static Random random;
  
  public static int[][] COMBINATION = new int[][] {
    // line 1
    { 1, 4, 7, 10, 13 },
    // line 2
    { 0, 3, 6, 9, 12 },
    // line 3
    { 2, 5, 8, 11, 14 },
    // line 4
    { 0, 4, 8, 10, 12 },
    // line 5
    { 2, 4, 6, 10, 14 },
    // line 6
    { 0, 3, 7, 11, 14 },
    // line 7
    { 2, 5, 7, 9, 12 },
    // line 8
    { 1, 5, 7, 9, 13 },
    // line 9
    { 1, 3, 7, 11, 13 },
  };
  
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
    { 0, 0, 100, 200, 1750 }
  };
  
	public static void Init() {
		Util.log("SlotCombinations - Init");
  	random = new Random();
  }
    
  public static int RandomItem() {
    float cap = 0;
    randomValue = random.nextFloat() * 100;
    for (int i = 0; i < ITEM_RATES.length; i++) {
      if (randomValue <= cap + ITEM_RATES[i]) {
        return i;
      } else {
        cap += ITEM_RATES[i];
      }
    }
    return 1;
  }
  
  public static int[] GenerateRandomItems() {
  	int[] resultData = new int[MAX_DISPLAY_ITEMS];
  	for (int i = 0; i < MAX_DISPLAY_ITEMS; i++) {
  		resultData[i] = RandomItem();
    }
  	return resultData;
  }
  
  // input data is array type of 15 items - output data is array winning gold of 9 lines
  public static JSONObject CalculateCombination(int[] reelData, int numLines, int betPerLine) {
  	JSONObject results = new JSONObject();
    int[] winningLineCount = new int[numLines];
    int[] winningLineType = new int[numLines];
    int[] winningGold = new int[numLines];
    boolean isJackpot = false; 
    for (int i = 0; i < numLines; i++) {
      for (int j = 0; j < NUM_REELS - 1; j++) {
        if (j == 0 && reelData[COMBINATION[i][j]] != SlotItem.WILD) {
          winningLineCount[i]++;
          winningLineType[i] = reelData[COMBINATION[i][j]];
          continue;
        }
        if (reelData[COMBINATION[i][j]] == SlotItem.WILD) {
          winningLineCount[i]++;
        } else {
          if (winningLineType[i] == 0) {
            winningLineCount[i]++;
            winningLineType[i] = reelData[COMBINATION[i][j]];
            continue;
          } else if (reelData[COMBINATION[i][j]] == winningLineType[i]){
            winningLineCount[i]++;
            continue;
          } else {
            break;
          }
        }
      }
      if (numLines == MAX_LINE && winningLineCount[i] == NUM_REELS) {
      	isJackpot = true;
      }
      winningGold[i] = PAYOUTS[winningLineType[i]][winningLineCount[i] - 1] * betPerLine;
    }
    try {
			results.put("winningGold", new JSONArray(winningGold));
	    results.put("isJackpot", isJackpot);
		} catch (JSONException e) {
			Util.log("CalculateCombination JSONObject error: " + e.toString());
		}
    return results;
  }
}
