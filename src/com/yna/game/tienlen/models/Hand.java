package com.yna.game.tienlen.models;

import java.util.ArrayList;
import java.util.Collections;

import org.uncommons.maths.combinatorics.CombinationGenerator;

public class Hand extends CardSet {
	public class ArrangeType {
		public static final int RANDOM = 0;
		public static final int SET_DESC = 1;
		public static final int SET_ASC = 2;
		public static final int CARD_DESC = 3;
		public static final int CARD_ASC = 4;
	}
	
	public static final int MAX_CARDS = 13;
	public ArrayList<Combination> combinations;
	
	public Hand() {
		super();
		combinations = new ArrayList<Combination>();
	}
	
	public Hand(ArrayList<Card> cards) {
		super(cards);
		combinations = new ArrayList<Combination>();
	}
	
	public boolean canDrop(String cardsString) {
		Combination combination = new Combination(cardsString);
		
		if (combination.numCards != 1 && combination.type == Combination.Type.NOTHING) {
			return false;
		}
		
		return true;
	}
	
	public ArrayList<Combination> findCombinations() {
		combinations.clear();
		ArrayList<Card> tempCards = new ArrayList<Card>(cards); 
		CombinationGenerator<Card> generator = null;
		int combinationLength = MAX_CARDS;
		Combination combination = null;
		
		while (combinationLength > 1) {
			boolean hasFoundCombination = false;
			generator = new CombinationGenerator<Card>(tempCards, combinationLength);
			
			while (generator.hasMore()) {
				ArrayList<Card> set = (ArrayList<Card>)generator.nextCombinationAsList();
				combination = new Combination(set);
				
				if (combination.type != Combination.Type.NOTHING) {
					combinations.add(combination);
					
					for (int i = 0; i < set.size(); i++) {
						tempCards.remove(set.get(i));
					}
					
					combinationLength = tempCards.size();
					hasFoundCombination = true;
					break;
				}
			}
			
			if (!hasFoundCombination) {
				combinationLength--;
			}
		}
		
		// Only single cards left.
		if (tempCards.size() > 0) {
			combination = new Combination(tempCards);
			combination.findType();
			combinations.add(combination);
		}
		
		generator = null;
		return combinations;
	}
	
	// Always find combinations before this.
	public void arrange(int arrangeType) {
		switch (arrangeType) {
			case ArrangeType.RANDOM:
				Collections.shuffle(cards);
				break;
			case ArrangeType.SET_DESC:
				cards.clear();
				
				for (int i = 0; i < combinations.size(); i++) {
					combinations.get(i).sort(-1);
					cards.addAll(combinations.get(i).cards);
				}
				break;
			case ArrangeType.SET_ASC:
				cards.clear();
				
				for (int i = combinations.size() - 1; i >= 0; i--) {
					combinations.get(i).sort(1);
					cards.addAll(combinations.get(i).cards);
				}
				break;
			case ArrangeType.CARD_ASC:
				sort(SortType.ASC);
				break;
			case ArrangeType.CARD_DESC:
				sort(SortType.DESC);
				break;
		}
	}
}