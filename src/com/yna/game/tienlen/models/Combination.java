package com.yna.game.tienlen.models;

import java.util.ArrayList;

import com.yna.game.tienlen.models.Card.Rank;

public class Combination extends CardSet {
	public class Type {
		public static final int NOTHING = 0;
		public static final int TWO = 1;
		public static final int COUPLE = 2;
		public static final int TRIPLE = 3;
		public static final int QUAD = 4;
		public static final int STRAIGHT = 5;
		public static final int THREE_COUPLES = 6;
		public static final int FOUR_COUPLES = 7;
		public static final int DOUBLE_QUAD = 8;
		public static final int FIVE_COUPLES = 9;
		public static final int SIX_COUPLES = 10;
		public static final int TRIPLE_QUAD = 11;
	}
	
	public int type;
	public boolean aceSpecial;
	public boolean twoSpecial;
	
	public Combination() {
		super();
		type = Type.NOTHING; 
	}
	
	public Combination(ArrayList<Card> cards) {
		super(cards);
		findType();
	}
	
	public Combination(String cardsString) {
		super(cardsString);
		findType();
	}
	
	public void findType() {
		sort(SortType.ASC);
		
		type = Type.NOTHING;
		aceSpecial = false; 
		twoSpecial = false;
		
		switch (numCards) {
			case 1:
				if (at(0).rank == Rank.TWO) {
					type = Type.TWO;
				}
				break;
			case 2:
				Card first = at(0);
				Card last = at(1);
				
				if (isCouple(first, last)) {
					type = Type.COUPLE;
					twoSpecial = first.rank == Card.Rank.TWO;
				}
				break;
			case 3:
				first = at(0);
				last = at(2);
				
				if (first.hasSameRank(last)) {
					type = Type.TRIPLE;
					twoSpecial = first.rank == Card.Rank.TWO;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				}
				break;
			case 4:
				first = at(0);
				last = at(3);
				
				if (first.hasSameRank(last)) {
					type = Type.QUAD;
					twoSpecial = first.rank == Card.Rank.TWO;
					aceSpecial = first.rank == Card.Rank.ACE;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				}
				break;
			case 5:
			case 7:
			case 9:
			case 11:
			case 13:
				if (isStraight()) {
					type = Type.STRAIGHT;
				}
				break;
			case 6:
				if (isCouple(at(0), at(1)) && isCouple(at(2), at(3)) && isCouple(at(4), at(5)) &&
					at(0).hasSameColor(at(2)) && at(0).hasSameColor(at(4)) && at(4).rank - at(0).rank == 2) {
					type = Type.THREE_COUPLES;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				}
				break;
			case 8:
				if (isCouple(at(0), at(1)) && isCouple(at(2), at(3)) && isCouple(at(4), at(5)) && isCouple(at(6), at(7)) &&
					at(0).hasSameColor(at(2)) && at(0).hasSameColor(at(4)) && at(0).hasSameColor(at(6)) && at(6).rank - at(0).rank == 3) {
					type = Type.FOUR_COUPLES;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				} else if (at(0).rank == at(3).rank && at(4).rank == at(7).rank) {
					type = Type.DOUBLE_QUAD;
				}
				break;
			case 10:
				if (isCouple(at(0), at(1)) && isCouple(at(2), at(3)) && isCouple(at(4), at(5)) && isCouple(at(6), at(7)) &&
					isCouple(at(8), at(9)) && at(0).hasSameColor(at(2)) && at(0).hasSameColor(at(4)) &&
					at(0).hasSameColor(at(6)) && at(0).hasSameColor(at(8)) && at(8).rank - at(0).rank == 4) {
					type = Type.FIVE_COUPLES;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				}
				break;
			case 12:
				if (isCouple(at(0), at(1)) && isCouple(at(2), at(3)) && isCouple(at(4), at(5)) && isCouple(at(6), at(7)) &&
					isCouple(at(8), at(9)) && isCouple(at(10), at(11)) && at(0).hasSameColor(at(2)) && 
					at(0).hasSameColor(at(4)) && at(0).hasSameColor(at(6)) && at(0).hasSameColor(at(8)) && 
					at(0).hasSameColor(at(10)) && at(10).rank - at(0).rank == 5) {
					type = Type.SIX_COUPLES;
				} else if (isStraight()) {
					type = Type.STRAIGHT;
				} else if (at(0).rank == at(3).rank && at(4).rank == at(7).rank && at(8).rank == at(11).rank) {
					type = Type.TRIPLE_QUAD;
				}
				break;
		}
	}
	
	public boolean canDefeat(Combination another) {
		switch (another.type) {
			case Type.NOTHING:
				switch (type) {
					case Type.NOTHING: return numCards == 1 && at(0).suit == another.at(0).suit && at(0).rank > another.at(0).rank;
					case Type.TWO: return true;
					default: return false;
				}
			case Type.TWO:
				switch (type) {
					case Type.TWO: return at(0).suit > another.at(0).suit;
					case Type.QUAD:
					case Type.THREE_COUPLES:
					case Type.FOUR_COUPLES:
					case Type.FIVE_COUPLES:
					case Type.SIX_COUPLES:
						return true;
					default: return false; 
				}
			case Type.COUPLE:
				switch (type) {
					case Type.COUPLE:
						if (another.twoSpecial) {
							return twoSpecial && at(1).suit == Card.Suit.HEART;  
						} else {
							return twoSpecial || (at(0).hasSameColor(another.at(0)) && at(0).rank > another.at(0).rank); 
						}
					case Type.QUAD:
						return another.twoSpecial && aceSpecial;
					case Type.FOUR_COUPLES:
					case Type.FIVE_COUPLES:
					case Type.SIX_COUPLES:
					case Type.DOUBLE_QUAD:
						return another.twoSpecial;
					default: return false;
				}
			case Type.TRIPLE: 
				return type == Type.TRIPLE && at(0).rank > another.at(0).rank && 
						at(0).suit == another.at(0).suit && at(1).suit == another.at(1).suit && at(2).suit == another.at(2).suit;
			case Type.QUAD:
				return type == Type.QUAD && at(0).rank > another.at(0).rank;
			case Type.STRAIGHT:
				return type == Type.STRAIGHT && numCards >= another.numCards && at(0).suit == another.at(0).suit && at(0).rank > another.at(another.numCards - 1).rank;
			case Type.THREE_COUPLES:
				return type == Type.THREE_COUPLES && at(0).hasSameColor(another.at(0)) && at(0).rank > another.at(0).rank;
			case Type.DOUBLE_QUAD:
				return type == Type.DOUBLE_QUAD && at(0).rank > another.at(0).rank && at(4).rank > another.at(4).rank;
				
		}
		
		return false;
	}
	
	private static boolean isCouple(Card card1, Card card2) {
		return (card1.rank == Card.Rank.TWO || card1.hasSameColor(card2)) && card1.hasSameRank(card2);
	}
	
	private boolean isStraight() {
		if (at(numCards - 1).rank - at(0).rank != numCards - 1) {
			return false;
		}
		
		for (int i = 0; i < numCards - 1; i++) {
			if (at(i).suit != at(i + 1).suit) {
				return false;
			}
		}
		
		return true;
	}
	
	public String getTypeString() {
		String typeString = "Nothing";
		
		switch (type) {
			case Type.COUPLE: typeString = "Couple"; break;
			case Type.TRIPLE: typeString = "Triple"; break;
			case Type.QUAD: typeString = "Quad"; break;
			case Type.STRAIGHT: typeString = "Straight"; break;
			case Type.THREE_COUPLES: typeString = "Three couples"; break;
			case Type.FOUR_COUPLES: typeString = "Four couples"; break;
			case Type.FIVE_COUPLES: typeString = "Five couples"; break;
			case Type.SIX_COUPLES: typeString = "Six couples"; break;
		}
		
		return typeString;
	}
	
	public static boolean canDefeat(CardSet currentSet, CardSet previousSet) {
		Combination currentCombination = new Combination(currentSet.cards);
		Combination previousCombination = new Combination(previousSet.cards);
		return currentCombination.canDefeat(previousCombination);
	}
}
