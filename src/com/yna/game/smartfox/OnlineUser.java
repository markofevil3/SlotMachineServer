package com.yna.game.smartfox;

import org.json.JSONException;
import org.json.JSONObject;

import com.yna.game.common.GameConstants;
import com.yna.game.common.Util;

public class OnlineUser {
	
	public JSONObject jsonData;
	public long expiredAt = 0;
	public long lastSavedToDB = 0;
	public String username;
	
	public OnlineUser(JSONObject jsonData) {
		try {
			this.jsonData = jsonData;
			this.username = jsonData.getString("username");
			this.lastSavedToDB = System.currentTimeMillis();
		} catch (JSONException e) {
			Util.log("-----------new OnlineUser JSONException : " + e.toString());
		}
	}
	
	public boolean ShouldSaveToDB() {
		if (lastSavedToDB + GameConstants.SAVE_TO_DB_INTERVAL_MILI >= System.currentTimeMillis()) {
			return true;
		}
		return false;
	}
	
	public void SetLastSavedToDB(long lastSaveTime) {
		this.lastSavedToDB = lastSaveTime;
	}
	
	public boolean IsExpired() {
		if (expiredAt > 0 && expiredAt >= System.currentTimeMillis()) {
			return true;
		}
		return false;
	}
	
	public void SetExpiredTime(long expiredAt) {
		this.expiredAt = expiredAt;
	}
	
	public void ClearExpiredTime() {
		expiredAt = 0;
	}
}
