package com.yna.game.tienlen.models;

import org.json.JSONObject;

import com.yna.game.common.Util;

public class GameConfig {
	public class Type {
		public static final int INVALID = -1;
		public static final int NORMAL = 0;
		public static final int COUNT = 1;
	}

	public int type;
	public int coinPerCard;
	public int firstRankRewardCoin;
	public int secondRankRewardCoin;
	
	public GameConfig(int coinPerCard) {
		this.type = Type.COUNT;
		this.coinPerCard = coinPerCard;
	}
	
	public GameConfig(int firstRankRewardCoin, int secondRankRewardCoin) {
		this.type = Type.NORMAL;
		this.firstRankRewardCoin = firstRankRewardCoin;
		this.secondRankRewardCoin = secondRankRewardCoin;
	}
	
	public GameConfig(JSONObject jsonData) {
		try {
			type = jsonData.getInt("type");
			coinPerCard = jsonData.getInt("coinPerCard");
			firstRankRewardCoin = jsonData.getInt("firstRankRewardCoin");
			secondRankRewardCoin = jsonData.getInt("secondRankRewardCoin");
		} catch (Exception exception) {
			type = Type.INVALID;
		}
	}
	
	public JSONObject toJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("type", type);
			jsonData.put("coinPerCard", coinPerCard);
			jsonData.put("firstRankRewardCoin", firstRankRewardCoin);
			jsonData.put("secondRankRewardCoin", secondRankRewardCoin);
		} catch (Exception exception) {
			Util.log("GameConfig:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
	
	public boolean isUserAffordable(int availableCoin) {
		switch (type) {
			case Type.COUNT: return availableCoin >= Hand.MAX_CARDS * coinPerCard;
			case Type.NORMAL: return availableCoin >= firstRankRewardCoin;
			default: return false;
		}
	}
	
	public boolean isValid() {
		return type != Type.INVALID;
	}
}
