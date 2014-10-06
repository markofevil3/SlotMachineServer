package com.yna.game.tienlen.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CardSet {
	public class SortType {
		public static final int ASC = 1;
		public static final int DESC = -1;
	}
	
	public ArrayList<Card> cards;
	public int numCards;

	public CardSet() {
		cards = new ArrayList<Card>();
		numCards = 0;
	}
	
	public CardSet(String cardsString) {
		this();
		addCards(cardsString);
	}
	
	public CardSet(ArrayList<Card> cards) {
		this.cards = cards;
		numCards = cards.size();
	}
	
	public void addCard(Card card) {
		cards.add(card);
		numCards++;
	}
	
	public void addCards(String cardsString) {
		cardsString = cardsString.replace(" ", "");
		int count = cardsString.length() / 2;
		
		for (int i = 0; i < count; i++) {
			addCard(new Card(cardsString.substring(i * 2, i * 2 + 2)));
		}
	}
	
	public boolean contains(String cardsString) {
		cardsString = cardsString.replace(" ", "");
		int count = cardsString.length() / 2;
		
		for (int i = 0; i < count; i++) {
			if (!contains(new Card(cardsString.substring(i * 2, i * 2 + 2)))) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean contains(Card card) {
		for (int i = 0; i < cards.size(); i++) {
			if (at(i).index == card.index) {
				return true;
			}
		}
		
		return false;
	}
	
	public void removeCard(Card card) {
		for (int i = 0; i < cards.size(); i++) {
			if (at(i).index == card.index) {
				cards.remove(i);
				numCards--;
				break;
			}
		}
	}
	
	public void removeCard(int cardIndex) {
		for (int i = 0; i < cards.size(); i++) {
			if (at(i).index == cardIndex) {
				cards.remove(i);
				numCards--;
				break;
			}
		}
	}
	
	public void removeCard(String cardString) {
		Card card = new Card(cardString);
		removeCard(card.index);
	}
	
	public void removeCards(String cardsString) {
		cardsString = cardsString.replace(" ", "");
		int count = cardsString.length() / 2;
		Card card = null;
		
		for (int i = 0; i < count; i++) {
			card = new Card(cardsString.substring(i * 2, i * 2 + 2));
			removeCard(card.index);
		}
	}
	
	public void removeAt(int index) {
		if (index < numCards) {
			numCards--;
			cards.remove(index);
		}
	}
	
	public int getNumCards() {
		return numCards;
	}
	
	public boolean isEmpty() {
		return numCards == 0;
	}
	
	public Card at(int index) {
		return index < numCards ? cards.get(index) : null;
	}
	
	public String toCardString() {
		if (numCards == 0) {
			return "";
		}
		
		String st = "";
		
		for (int i = 0; i < numCards - 1; i++) {
			st += at(i).toString() + " ";
		}
		
		st += at(numCards - 1).toString();
		return st;
	}
	
	public void sort(int direction) {
		if (direction == SortType.ASC) {
			Collections.sort(cards, new Comparator<Card>() {
				public int compare(Card card1, Card card2) {
					return card1.compareTo(card2) * SortType.ASC;
				}
			});
		} else {
			Collections.sort(cards, new Comparator<Card>() {
				public int compare(Card card1, Card card2) {
					return card1.compareTo(card2) * SortType.DESC;
				}
			});
			
		}
	}
	
	public void reset() {
		cards.clear();
		numCards = 0;
	}
}