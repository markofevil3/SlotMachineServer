package com.yna.game.smartfox;

import org.json.JSONObject;

public class AdminMessage {
	
	public int id;
	public JSONObject message;
	public long createdAt;
	public long expiredAt;
	public int targetType;
	public String[] usernames;
	
	public AdminMessage(int id, JSONObject message, long createdAt, long expiredAt, int targetType, String usernames) {
		this.id = id;
		this.message = message;
		this.createdAt = createdAt;
		this.expiredAt = expiredAt;
		this.targetType = targetType;
		this.usernames = usernames.trim().split(",");
	}
}
