package com.yna.game.tienlen.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import org.uncommons.maths.combinatorics.CombinationGenerator;

import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class Simulator {
	public static void main(String args[]) {
//		Util.log("Start testing");
		
//		testCardClass();
//		testCombinationClass();
//		testCombinationGenerator();
//		testHand();
//		testDeck();
//		testGame();
	}
	
	private static void testGame() {
		BufferedReader bufferReader;
		GameConfig gameConfig = new GameConfig(10);
		GameRoom gameSet = new GameRoom("test", gameConfig);
		
		for (int i = 0; i < GameRoom.NUM_USERS; i++) {
			gameSet.initUser("user" + i, i, i == 0);
		}
		
		gameSet.play();
		int seatTurnIndex = gameSet.activeUserSeatIndex;
		UserGame userGame = gameSet.getUserGameBySeatIndex(seatTurnIndex);
		
		while (true) {
			try {
				bufferReader = new BufferedReader(new InputStreamReader(System.in));
				String input = bufferReader.readLine();
				
				if (input.equals("quit")) {
					break;
				}
				
				if (input.equals("fold")) {
					gameSet.fold(userGame.userId);
					gameSet.print();
					seatTurnIndex = gameSet.activeUserSeatIndex;
					userGame = gameSet.getUserGameBySeatIndex(seatTurnIndex);
				} else {
					int errorCode = gameSet.drop(userGame.userId, input);
					Util.log("errorCode:" + errorCode);
					
					switch (errorCode) {
					case ErrorCode.Tienlen.NULL:
						seatTurnIndex = gameSet.activeUserSeatIndex;
						userGame = gameSet.getUserGameBySeatIndex(seatTurnIndex);
						gameSet.check();
						gameSet.print();
						break;
					}
				}
			} catch (Exception exception) {
				Util.log(exception.toString());
			}
		}
	}
	
	private static void testDeck() {
		Deck deck = new Deck();
		Util.log(deck.toCardString());
		Util.log(deck.deal().toString());
		Util.log(deck.toCardString());
	}
	
	private static void testHand() {
		Hand hand = new Hand();
		hand.addCards("AH KH QH JH TH 5S 5H 4C 4S 3S8H 8C 2H ");
		Util.log(hand.toCardString());
		ArrayList<Combination> combinations = hand.findCombinations();
		for (int i = 0; i < combinations.size(); i++) {
			Combination combination = combinations.get(i);
			Util.log(combination.toCardString() + " " + combination.getTypeString());
		}
		hand.arrange(Hand.ArrangeType.SET_ASC);
		Util.log(hand.toCardString());
		hand.arrange(Hand.ArrangeType.SET_DESC);
		Util.log(hand.toCardString());
	}
	
	private static void testCombinationGenerator() {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(new Card("AH"));
		cards.add(new Card("AD"));
		cards.add(new Card("AC"));
		CombinationGenerator<Card> generator = new CombinationGenerator<Card>(cards, 2);
		Util.log("total:" + generator.getTotalCombinations());
		Util.log("cards:" + cards.toString());
		
		while (generator.hasMore()) {
			ArrayList<Card> aList = (ArrayList)generator.nextCombinationAsList();
			Util.log("aList:" + aList.toString());
		}
		
		ArrayList<Card> tempCards = new ArrayList<Card>(cards);
		Util.log("tempCards:" + tempCards.toString());
		tempCards.remove(0);
		Util.log("tempCards:" + tempCards.toString() + " cards:" + cards.toString());
	}
	
	private static void testCardClass() {
		Card card = new Card(20);
		Util.log(card.toString());
		
		Card card2 = new Card("2H");
		Util.log("index:" + card2.toIndex());
	}
	
	private static void testCombinationClass() {
		Combination combination = new Combination();
		combination.addCard(new Card("AH"));
		combination.addCard(new Card("KH"));
		combination.addCard(new Card("QH"));
		combination.addCard(new Card("JH"));
		combination.addCard(new Card("TH"));
//		combination.addCard(new Card("2D"));
		combination.findType();
		Util.log(combination.toCardString() + " " + combination.getTypeString());
		
		Combination combination2 = new Combination();
		combination2.addCard(new Card("6H"));
		combination2.addCard(new Card("6D"));
		combination2.addCard(new Card("6C"));
		combination2.addCard(new Card("6S"));
//		combination2.addCard(new Card("5H"));
		combination2.findType();
		Util.log(combination2.toCardString() + " " + combination2.getTypeString());
		
		Util.log("Defeat:" + combination.canDefeat(combination2));
	}
}