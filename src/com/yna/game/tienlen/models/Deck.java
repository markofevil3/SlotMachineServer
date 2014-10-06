package com.yna.game.tienlen.models;

import java.util.Collections;

public class Deck extends CardSet {
	public static final int NUM_CARDS = 52;
	
	public Deck() {
		super();
		init();
	}
	
	private void init() {
		for (int i = 0; i < NUM_CARDS; i++) {
			addCard(new Card(i));
		}
		
		shuffle();
	}
	
	private void shuffle() {
		Collections.shuffle(cards);
	}
	
	public Card deal() {
		if (numCards > 0) {
			Card card = at(0);
			removeAt(0);
			return card;
		}
		
		return null;
	}
}
