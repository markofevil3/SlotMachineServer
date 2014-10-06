package com.yna.game.tienlen.models;

import org.json.JSONObject;

import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class UserGame {
	public class State {
		public static final int WAITING = 0;
		public static final int PLAYING = 1;
	}
	
	public String userId;
	public int seatIndex;
	public int roundIndex;
	public int state;
	public boolean isHost;
	public boolean isActive;
	public boolean isHandFinished;
	public boolean hasJoinedThisRound;
	public Hand hand;
	
	public UserGame(String userId, int seatIndex, boolean isHost) {
		this.userId = userId;
		this.seatIndex = seatIndex;
		this.isHost = isHost;
		hand = new Hand();
		isActive = true;
		isHandFinished = false;
		hasJoinedThisRound = false;
		state = State.WAITING;
	}
	
	public JSONObject toJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("userId", userId);
			jsonData.put("isHost", isHost);
			jsonData.put("seatIndex", seatIndex);
			jsonData.put("isActive", isActive);
			jsonData.put("isHandFinished", isHandFinished);
			jsonData.put("state", state);
			jsonData.put("cards", hand.toCardString());
		} catch (Exception exception) {
			Util.log("UserGame:toJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
	
	public JSONObject forOtherToJson() {
		JSONObject jsonData = new JSONObject();
		
		try {
			jsonData.put("userId", userId);
			jsonData.put("isHost", isHost);
			jsonData.put("seatIndex", seatIndex);
			jsonData.put("isActive", isActive);
			jsonData.put("isHandFinished", isHandFinished);
			jsonData.put("state", state);
			jsonData.put("numCards", hand.getNumCards());
		} catch (Exception exception) {
			Util.log("UserGame:forOtherToJson:Exception:" + exception.toString());
		}
		
		return jsonData;
	}
	
	public void changeSeat(int newSeatIndex) {
		seatIndex = newSeatIndex;
	}
	
	public void addCards(String cardsString) {
		hand.addCards(cardsString);
	}
	
	public void addCard(Card card) {
		hand.addCard(card);
	}
	
	public void sortHand() {
		hand.sort(CardSet.SortType.DESC);
	}
	
	public int drop(CardSet lastDroppedCards, String cardsString) {
		if (!hand.contains(cardsString)) {
			return ErrorCode.Tienlen.CARDS_NOT_EXIST;
		} else if (!hand.canDrop(cardsString)) {
			return ErrorCode.Tienlen.CANNOT_DROP;
		}
		
		if (lastDroppedCards == null || lastDroppedCards.isEmpty() || Combination.canDefeat(new CardSet(cardsString), lastDroppedCards)) {
			hand.removeCards(cardsString);
			hasJoinedThisRound = true;
			return ErrorCode.Tienlen.NULL;
		}
		
		return ErrorCode.Tienlen.CANNOT_DEFEAT;
	}
	
	public void reset() {
		hand.reset();
		hasJoinedThisRound = false;
		isHandFinished = false;
	}
	
	public boolean isEmpty() {
		return hand.isEmpty();
	}
	
	public boolean isPlaying() {
		return !hand.isEmpty() && isActive;
	}
	
	public boolean isAtSeat(int seatIndex) {
		return this.seatIndex == seatIndex;
	}
	
	public boolean isUser(String userId) {
		return this.userId.equals(userId);
	}
	
	public boolean hasJoinThisRound() {
		return hasJoinedThisRound;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public boolean canPlayThisRound(int roundIndex) {
		return this.roundIndex == roundIndex;
	}
	
	public void start() {
		state = State.PLAYING;
	}
	
	public void waitToPlay() {
		state = State.WAITING;
	}
	
	public void setRound(int roundIndex) {
		this.roundIndex = roundIndex;
		hasJoinedThisRound = false;
	}
	
	public void finishHand() {
		isHandFinished = true;
		setRound(-1);
	}
	
	public void fold() {
		roundIndex = -1;
	}
	
	public void print() {
		Util.log(userId + " " + seatIndex + " " + roundIndex + " " + hand.toCardString());
	}
}
