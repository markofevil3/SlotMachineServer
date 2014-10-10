package com.yna.game.common;

public class ErrorCode {
	public static final String PARAM = "errorCode";
	
	public class User {
		public static final int NULL = 0;
		public static final int USER_EXIST = 1;
		public static final int USER_NOT_EXIST = 2;
		public static final int PASSWORD_NOT_MATCH = 3;
		public static final int MAX_FRIENDS = 4;
		public static final int CANT_FIND_USER = 5;
		public static final int ALREADY_FRIEND = 6;
	}
	
	public class Tienlen {
		public static final int NULL = 0;
		public static final int WRONG_TURN = 1;
		public static final int CARDS_NOT_EXIST = 2;
		public static final int CANNOT_DEFEAT = 3;
		public static final int CANNOT_DROP = 3;
		public static final int INVALID_GAMECONFIG = 4;
		public static final int CANNOT_CREATE_ROOM = 5;
		public static final int GAME_NOT_EXISTS = 6;
		public static final int UNKNOWN = 100;
	}
	
	public class SlotMachine {
		public static final int NULL = 0;
		public static final int INVALID_GAMECONFIG = 1;
		public static final int CANNOT_CREATE_ROOM = 2;
		public static final int GAME_NOT_EXISTS = 3;
		public static final int UNKNOWN = 100;
	}
}