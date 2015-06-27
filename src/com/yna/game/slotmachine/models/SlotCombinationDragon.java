package com.yna.game.slotmachine.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.yna.game.common.Util;

public class SlotCombinationDragon extends SlotCombinations {
 
	public static final String DATA_FILE_PATH = "/data/SlotMachineDragon.txt";

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
  
//  private static final int DRAGON_FIRE = 0;
//	private static final int DRAGON_ICE = 1;
//	private static final int DRAGON_DARK = 2;

	private static int[] BOSS_HP = new int[] { 5000000, 7000000, 10000000 };
	
  private static int[][] BOSS_DROP_CASH = new int[][] {
    // BOSS_FIRE
    { 100000, 130000, 150000 },
    // BOSS_ICE
    { 160000, 180000, 200000 },
    // BOSS_DARK
    { 300000, 350000, 400000 },
  };
  
  private static int[][] BOSS_DROP_GEM = new int[][] {
    // BOSS_FIRE
    { 0, 0, 1 },
    // BOSS_ICE
    { 0, 1, 2 },
    // BOSS_DARK
    { 1, 2, 3 },
  };
  
  private static int[] BET_PER_LINES = new int[] {1, 2, 3, 4, 5};

  public int[] GetBossHp() {
  	return BOSS_HP;
  }
  
  public int[][] GetBossDropCash() {
  	return BOSS_DROP_CASH;
  }
  
  public int[][] GetBossDropGem() {
  	return BOSS_DROP_GEM;
  }
  
  public int[][] GetPayOuts() {
  	return PAYOUTS;
  }
  
  public int[] GetItemRate() {
  	return ITEM_RATES;
  }
  
  public int[] GetSpecialItemRate() {
  	return SPECIAL_ITEM_RATES;
  }
  
  public int[] GetBetPerLines() {
  	return BET_PER_LINES;
  }
  
  public SlotCombinationDragon() {
  	Util.log("SlotCombinationDragon Init");
  	random = new Random();
  	initGameData();
  }
	
	public static void initGameData() {
		try {

			BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + DATA_FILE_PATH));
			String line = bufferedReader.readLine();
			Util.log("-----UPDATE SLOT DRAGON DATA-----");
			while (line != null) {
				String[] arr = line.split("=");
				if (arr.length >= 2) {
					switch(arr[0].trim()) {
						case "BET_PER_LINES":
							Util.StringToIntArray(arr[1].trim(), BET_PER_LINES);
							Util.log("BET_PER_LINES " + Util.IntArrayToString(BET_PER_LINES));
							break;
						case "ITEM_RATES":
							Util.StringToIntArray(arr[1].trim(), ITEM_RATES);
							Util.log("ITEM_RATES " + Util.IntArrayToString(ITEM_RATES));
							break;
						case "SPECIAL_ITEM_RATES":
							Util.StringToIntArray(arr[1].trim(), SPECIAL_ITEM_RATES);
							Util.log("SPECIAL_ITEM_RATES " + Util.IntArrayToString(SPECIAL_ITEM_RATES));
							break;
						case "BOSS_HP":
							Util.StringToIntArray(arr[1].trim(), BOSS_HP);
							Util.log("BOSS_HP " + Util.IntArrayToString(BOSS_HP));
							break;
						case "BOSS_DROP_GOLD1":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_CASH[0]);
							Util.log("BOSS_DROP_GOLD1 " + Util.IntArrayToString(BOSS_DROP_CASH[0]));
							break;
						case "BOSS_DROP_GOLD2":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_CASH[1]);
							Util.log("BOSS_DROP_GOLD2 " + Util.IntArrayToString(BOSS_DROP_CASH[1]));
							break;
						case "BOSS_DROP_GOLD3":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_CASH[2]);
							Util.log("BOSS_DROP_GOLD3 " + Util.IntArrayToString(BOSS_DROP_CASH[2]));
							break;
						case "BOSS_DROP_GEM1":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_GEM[0]);
							Util.log("BOSS_DROP_GEM1 " + Util.IntArrayToString(BOSS_DROP_GEM[0]));
							break;
						case "BOSS_DROP_GEM2":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_GEM[1]);
							Util.log("BOSS_DROP_GEM2 " + Util.IntArrayToString(BOSS_DROP_GEM[1]));
							break;
						case "BOSS_DROP_GEM3":
							Util.StringToIntArray(arr[1].trim(), BOSS_DROP_GEM[2]);
							Util.log("BOSS_DROP_GEM3 " + Util.IntArrayToString(BOSS_DROP_GEM[2]));
							break;
						case "PAYOUTS_ITEM_WILD":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[0]);
							Util.log("PAYOUTS_ITEM_WILD " + Util.IntArrayToString(PAYOUTS[0]));
							break;
						case "PAYOUTS_ITEM_1":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[1]);
							Util.log("PAYOUTS_ITEM_1 " + Util.IntArrayToString(PAYOUTS[1]));
							break;
						case "PAYOUTS_ITEM_2":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[2]);
							Util.log("PAYOUTS_ITEM_2 " + Util.IntArrayToString(PAYOUTS[2]));
							break;
						case "PAYOUTS_ITEM_3":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[3]);
							Util.log("PAYOUTS_ITEM_3 " + Util.IntArrayToString(PAYOUTS[3]));
							break;
						case "PAYOUTS_ITEM_4":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[4]);
							Util.log("PAYOUTS_ITEM_4 " + Util.IntArrayToString(PAYOUTS[4]));
							break;
						case "PAYOUTS_ITEM_5":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[5]);
							Util.log("PAYOUTS_ITEM_5 " + Util.IntArrayToString(PAYOUTS[5]));
							break;
						case "PAYOUTS_ITEM_6":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[6]);
							Util.log("PAYOUTS_ITEM_6 " + Util.IntArrayToString(PAYOUTS[6]));
							break;
						case "PAYOUTS_ITEM_7":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[7]);
							Util.log("PAYOUTS_ITEM_7 " + Util.IntArrayToString(PAYOUTS[7]));
							break;
						case "PAYOUTS_ITEM_8":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[8]);
							Util.log("PAYOUTS_ITEM_8 " + Util.IntArrayToString(PAYOUTS[8]));
							break;
						case "PAYOUTS_ITEM_9":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[9]);
							Util.log("PAYOUTS_ITEM_9 " + Util.IntArrayToString(PAYOUTS[9]));
							break;
						case "PAYOUTS_ITEM_10":
							Util.StringToIntArray(arr[1].trim(), PAYOUTS[10]);
							Util.log("PAYOUTS_ITEM_10 " + Util.IntArrayToString(PAYOUTS[10]));
							break;
					}
				}
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
		} catch (IOException e) {
			Util.log("SlotCombinationDragon:IOException:"	+ e.toString());
		}
	}
}
