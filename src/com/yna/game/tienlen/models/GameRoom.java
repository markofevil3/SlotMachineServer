package com.yna.game.tienlen.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yna.game.common.ErrorCode;
import com.yna.game.common.TimeManager;
import com.yna.game.common.Util;

public class GameRoom {
	public class State {
		public static final int WAITING = 0;
		public static final int STARTING = 1;
		public static final int PLAYING = 2;
		public static final int FINISHING = 3;
		public static final int FINISHED = 4;
	}
	
	public static final int NUM_USERS = 4;
	public static final int ROOM_NAME_LENGTH = 10;
	public static final int MAX_USERS = 10;
	public static final int DROP_TIMEOUT = 30 * 1000;
	public static final int START_TIMEOUT = 30 * 1000;
	public static final int FINISHING_TIMEOUT = 10 * 1000;
	public static final int FINISHED_TIMEOUT = 10 * 1000;
	
	public String roomId;
	public ArrayList<UserGame> userGames;
	public ArrayList<String> watchingUserIds;
	public ArrayList<String> finishedUserIds;
	public CardSet droppedCards;
	public CardSet roundDroppedCards;
	public String activeUserId;
	public String lastWinnerId;
	public int activeUserSeatIndex = -1;
	public int lastWinnerSeatIndex = -1;
	public int lastFinishedHandSeatIndex = -1;
	public int state;
	private int roundIndex;
	private int numPlaying;
	private GameConfig config;
	private long lastActionTime;
	
	public GameRoom(String roomId, GameConfig config) {
		this.roomId = roomId;
		this.config = config;
		init();
	}
	
	private void init() {
		userGames = new ArrayList<UserGame>();
		watchingUserIds = new ArrayList<String>();
		finishedUserIds = new ArrayList<String>();
		droppedCards = new CardSet();
		roundDroppedCards = new CardSet();
		activeUserId = null;
		lastWinnerId = null;
		setState(State.WAITING);
	}
	
	public void destroy() {
		
	}
	
	public boolean isRoomMatch(String roomId) {
		return this.roomId.equals(roomId);
	}
	
	public boolean isUserAffordable(int availableCoin) {
		return config.isUserAffordable(availableCoin);
	}
	
	public boolean canStart() {
		return userGames.size() >= 2;
	}
	
	public boolean canJoin() {
		return userGames.size() < NUM_USERS;
	}
	
	public boolean isEmpty() {
		return userGames.size() == 0;
	}
	
	public void initUser(String userId, int seatIndex, boolean isHost) {
		// TODO: Check if seat is taken.
		userGames.add(new UserGame(userId, seatIndex, isHost));
	}
	
	public void standup(String userId) {
		UserGame userGame = getUserGameByUserId(userId);
		
		if (userGame != null) {
			boolean shouldNextTurn = activeUserSeatIndex == userGame.seatIndex;
			userGames.remove(userGame);
			decreaseNumPlaying();
			
			if (shouldNextTurn) {
				nextTurn();
			}
		}
	}
	
	public void setTurn() {
		activeUserSeatIndex = lastWinnerSeatIndex != -1 ? lastWinnerSeatIndex : 0;
		markTime();
	}
	
	public void nextTurn() {
		boolean foundNextUser = false;
		UserGame userGame = null;
		
		if (lastFinishedHandSeatIndex != -1) {
			activeUserSeatIndex = lastFinishedHandSeatIndex;
		}
		
		for (int i = 0; i < NUM_USERS; i++) {
			activeUserSeatIndex = (activeUserSeatIndex + 1) % NUM_USERS;
			userGame = getUserGameBySeatIndex(activeUserSeatIndex);
			
			if (userGame != null && userGame.isPlaying() && userGame.canPlayThisRound(roundIndex)) {
				foundNextUser = true;
				break;
			}
		}
		
		if (!foundNextUser) {
			nextRound(true);
			nextTurn();
		} else if (getNumActiveUsersInRound() == 1 && userGame.hasJoinThisRound()) {
			nextRound(true);
		}
		
		markTime();
	}
	
	private int getNumActiveUsersInRound() {
		int numUsers = 0;
		
		for (int i = 0; i < userGames.size(); i++) {
			if (userGames.get(i).canPlayThisRound(roundIndex)) {
				numUsers++;
			}
		}
		
		return numUsers;
	}
	
	public UserGame getUserGameBySeatIndex(int seatIndex) {
		for (int i = 0; i < userGames.size(); i++) {
			if (userGames.get(i).isAtSeat(seatIndex)) {
				return userGames.get(i);
			}
		}
		
		return null;
	}
	
	public UserGame getUserGameByUserId(String userId) {
		for (int i = 0; i < userGames.size(); i++) {
			if (userGames.get(i).isUser(userId)) {
				return userGames.get(i);
			}
		}
		
		return null;
	}
	
	public UserGame getCurrentActiveUser() {
		return getUserGameBySeatIndex(activeUserSeatIndex);
	}
	
	private void nextRound(boolean isCompulsary) {
		roundIndex++;
		
		for (int i = 0; i < userGames.size(); i++) {
			UserGame userGame = userGames.get(i);
			
			if (!userGame.isHandFinished && (isCompulsary || userGame.roundIndex != -1)) {
				userGame.setRound(roundIndex);
			}
		}
		
		roundDroppedCards.reset();
	}
	
	public int drop(String userId, String cardsString) {
		Util.log("Drop " + userId + " " + cardsString);
		int errorCode = ErrorCode.Tienlen.WRONG_TURN;
		UserGame userGame = getUserGameByUserId(userId);
		userGame.print();
		
		if (userGame != null && userGame.canPlayThisRound(roundIndex)) {
			errorCode = userGame.drop(roundDroppedCards, cardsString);
			
			if (errorCode == ErrorCode.Tienlen.NULL) {
				lastFinishedHandSeatIndex = -1;
				
				if (userGame.isEmpty()) {
					lastFinishedHandSeatIndex = userGame.seatIndex;
					userGame.finishHand();
					finishedUserIds.add(userId);
					decreaseNumPlaying();
					nextRound(false);
				}
			
				roundDroppedCards.reset();
				roundDroppedCards.addCards(cardsString);
				droppedCards.addCards(cardsString);
				
				nextTurn();
			}
		}
		
		return errorCode;
	}
	
	public void fold(String userId) {
		UserGame userGame = getUserGameByUserId(userId);
		
		if (userGame != null) {
			userGame.fold();
		}
		
		nextTurn();
	}
	
	public void check() {
		switch (config.type) {
		case GameConfig.Type.NORMAL:
			if (numPlaying == 1) {
				setState(State.FINISHING);
			}
			break;
		case GameConfig.Type.COUNT:
			if (numPlaying == userGames.size() - 1) {
				setState(State.FINISHING);
			}
			break;
		}
	}
	
	public void start() {
		if (!canStart()) {
			return;
		}
		
		Util.log("Start");
		Deck deck = new Deck();
		
		for (int i = 0; i < Hand.MAX_CARDS; i++) {
			for (int j = 0; j < userGames.size(); j++) {
				userGames.get(j).addCard(deck.deal());
			}
		}
		
		for (int j = 0; j < userGames.size(); j++) {
			userGames.get(j).sortHand();
			userGames.get(j).start();
		}
		
		setTurn();
		nextRound(true);
		setState(State.PLAYING);
//		setState(State.STARTING);
	}
	
	public void update() {
		switch (state) {
		case State.WAITING:
			break;
		case State.STARTING:
			break;
		case State.PLAYING:
			boolean shouldPublishGame = false;
			long now = TimeManager.GetTimeInMillis();
			JSONObject outData = new JSONObject();
			
			if (now > lastActionTime + DROP_TIMEOUT) {
				String currentUserId = getCurrentActiveUser().userId;
				fold(currentUserId);
				shouldPublishGame = true;
				Util.log("check:Playing:DROP_TIMEOUT:" + currentUserId);
				
				try {
					JSONObject updateJsonData = new JSONObject();
					updateJsonData.put("updateDataType", UpdateData.USER_FOLD);
					updateJsonData.put("userFold", currentUserId);
					updateJsonData.put("gameRoomUpdate", forAllToJson());
					outData.put("updateData", updateJsonData);
				} catch (Exception exception) {
					Util.log("check:Playing:Exception:" + exception.toString());
				}
			}
			
			check();
			
			if (shouldPublishGame) {
				GameManager.publishGame(this, null, outData);
			}
			break;
		case State.FINISHING:
			now = TimeManager.GetTimeInMillis();
			
			if (now > lastActionTime + FINISHING_TIMEOUT) {
				setState(State.FINISHED);
				GameManager.publishGame(this, null, new JSONObject());
			}
			break;
		case State.FINISHED:
			now = TimeManager.GetTimeInMillis();
			
			if (now > lastActionTime + FINISHING_TIMEOUT) {
				setState(State.STARTING);
				GameManager.publishGame(this, null, new JSONObject());
			}
			break;
		}
	}
	
	public void finish() {
		setState(State.FINISHED);
	}
	
	public void play() {
		Util.log("Play");
		setState(State.PLAYING);
		print();
	}
	
	public void reset() {
		for (int i = 0; i < userGames.size(); i++) {
			userGames.get(i).reset();
		}

		roundIndex = -1;
		droppedCards.reset();
		roundDroppedCards.reset();
		finishedUserIds.clear();
		setState(State.WAITING);
	}
	
	private void setState(int newState) {
		state = newState;
		
		switch (state) {
		case State.STARTING:
			numPlaying = userGames.size();
			break;
		}
		
		markTime();
	}
	
	private void markTime() {
		lastActionTime = TimeManager.GetTimeInMillis();
	}
	
	private void decreaseNumPlaying() {
		if (numPlaying > 1) {
			numPlaying--;
		}
	}
	
	public void print() {
		Util.log("State:" + state + " activeUserSeatIndex:" + activeUserSeatIndex + ", roundIndex:" + roundIndex + " " + "roundDroppedCards:" + roundDroppedCards.toCardString());
		
		for (int i = 0; i < userGames.size(); i++) {
			Util.log("GameRoom:print:" + i + " " + userGames.get(i));
			userGames.get(i).print();
		}
	}
	
	public JSONObject toJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("roomId", roomId);
			jsonData.put("state", state);
			jsonData.put("activeUserSeatIndex", activeUserSeatIndex);
			jsonData.put("dropTimeout", DROP_TIMEOUT);
			jsonData.put("droppedCards", droppedCards.toCardString());
			jsonData.put("roundDroppedCards", roundDroppedCards.toCardString());
			
			JSONArray userGameArray = new JSONArray();
			
			for (int i = 0; i < userGames.size(); i++) {
				userGameArray.put(userGames.get(i).toJson());
			}
			
			jsonData.put("userGames", userGameArray);
			jsonData.put("gameConfig", config.toJson());
		} catch (Exception exception) {
			Util.log("GameRoom:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
	
	public JSONObject forAllToJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("roomId", roomId);
			jsonData.put("state", state);
			jsonData.put("activeUserSeatIndex", activeUserSeatIndex);
			jsonData.put("dropTimeout", DROP_TIMEOUT);
			jsonData.put("droppedCards", droppedCards.toCardString());
			jsonData.put("roundDroppedCards", roundDroppedCards.toCardString());
			jsonData.put("gameConfig", config.toJson());
		} catch (Exception exception) {
			Util.log("GameRoom:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;

	}
	
	public JSONObject forUserToJson(String userId) {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("roomId", roomId);
			jsonData.put("state", state);
			jsonData.put("activeUserSeatIndex", activeUserSeatIndex);
			jsonData.put("dropTimeout", DROP_TIMEOUT);
			jsonData.put("droppedCards", droppedCards.toCardString());
			jsonData.put("roundDroppedCards", roundDroppedCards.toCardString());
			
			JSONArray userGameArray = new JSONArray();
			
			for (int i = 0; i < userGames.size(); i++) {
				UserGame userGame = userGames.get(i);
				
				if (userGame.isUser(userId)) {
					userGameArray.put(userGame.toJson());
				} else {
					userGameArray.put(userGame.forOtherToJson());
				}
			}
			
			jsonData.put("userGames", userGameArray);
			jsonData.put("gameConfig", config.toJson());
		} catch (Exception exception) {
			Util.log("GameRoom:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
	
	public JSONObject toLobbyJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("state", state);
			jsonData.put("gameConfig", config.toJson());
			jsonData.put("numUsers", userGames.size());
			jsonData.put("id", roomId);
		} catch (Exception exception) {
			Util.log("GameRoom:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
}