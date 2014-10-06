package com.yna.game.tienlen.models;

import com.smartfoxserver.v2.extensions.SFSExtension;

public class TienLenManager {
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