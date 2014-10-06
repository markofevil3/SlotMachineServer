package com.yna.game.poker.models;

import com.smartfoxserver.v2.extensions.SFSExtension;

public class PokerManager {
	public static void init(SFSExtension extension) {
		GameManager.init(extension);
	}
	
	public static void destroy() {
		GameManager.destroy();
	}
	
	public static void update() {
		GameManager.update();
	}
}