package com.yna.game.tienlen.models;

//
// 3S =  0    3C = 13    3D = 26    3H = 39
// 4S =  1    4C = 14    4D = 27    4H = 40
// 5S =  2    5C = 15    5D = 28    5H = 41
// 6S =  3    6C = 16    6D = 29    6H = 42
// 7S =  4    7C = 17    7D = 30    7H = 43
// 8S =  5    8C = 18    8D = 31    8H = 44
// 9S =  6    9C = 19    9D = 32    9H = 45
// TS =  7    TC = 20    TD = 33    TH = 46
// JS =  8    JC = 21    JD = 34    JH = 47
// QS =  9    QC = 22    QD = 35    QH = 48
// KS = 10    KC = 23    KD = 36    KH = 49
// AS = 11    AC = 24    AD = 37    AH = 50
// 2S = 12    2C = 25    2D = 38    2H = 51
//

public class Card {
	public static final int TOTAL = 52;
	public static final int NUM_SUITS = 4;
	public static final int NUM_RANKS = 13;
	public static final int INVALID = -1;
	
	public class Suit {
		public static final int SPADE = 0;
		public static final int CLUB = 1;
		public static final int DIAMOND = 2;
		public static final int HEART = 3;
	}
	
	public class Rank {
		public static final int THREE = 0;
		public static final int FOUR = 1;
		public static final int FIVE = 2;
		public static final int SIX = 3;
		public static final int SEVEN = 4;
		public static final int EIGHT = 5;
		public static final int NINE = 6;
		public static final int TEN = 7;
		public static final int JACK = 8;
		public static final int QUEEN = 9;
		public static final int KING = 10;
		public static final int ACE = 11;
		public static final int TWO = 12; 
	}
	
	public int suit;
	public int rank;
	public int index;
	
	public Card(int index) {
		this.index = index;
		this.rank = getRank();
		this.suit = getSuit();
	}
	
	public Card(String st) {
		if (st != null && st.length() == 2) {
			index = charsToIndex(st.charAt(0), st.charAt(1));
			this.rank = getRank();
			this.suit = getSuit();
		} else {
			index = INVALID;
		}
	}
	
	public int toIndex() {
		return suit * NUM_RANKS + rank;
	}
	
	public int getRank() {
		return index % NUM_RANKS;
	}
	
	public int getSuit() {
		return index / NUM_RANKS;
	}
	
	public boolean hasSameRank(Card another) {
		return rank == another.rank;
	}
	
	public boolean hasSameColor(Card another) {
		return (suit < Suit.DIAMOND && another.suit < Suit.DIAMOND) || 
			   (suit >= Suit.DIAMOND && another.suit >= Suit.DIAMOND) ||
			   (rank == Rank.TWO && another.rank == Rank.TWO); 
	}
	
	public String toString() {
		return toString(rank, suit);
	}
	
	public int compareTo(Card another) {
		if (rank < another.rank) {
			return -1;
		} else if (rank > another.rank) {
			return 1;
		}
		
		if (suit < another.suit) {
			return -1;
		} else if (suit > another.suit) {
			return 1;
		}
		
		return 0;
	}
	
	public static int toIndex(int rank, int suit) {
		return suit * NUM_RANKS + rank;
	}
	
	public static int charsToIndex(char rank, char suit) {
		int r = INVALID;
		
		switch (rank) {
			case '2': r = Rank.TWO; break;
			case '3': r = Rank.THREE; break;
			case '4': r = Rank.FOUR; break;
			case '5': r = Rank.FIVE; break;
			case '6': r = Rank.SIX; break;
			case '7': r = Rank.SEVEN; break;
			case '8': r = Rank.EIGHT; break;
			case '9': r = Rank.NINE; break;
			case 'T': r = Rank.TEN; break;
			case 'J': r = Rank.JACK; break;
			case 'Q': r = Rank.QUEEN; break;
			case 'K': r = Rank.KING; break;
			case 'A': r = Rank.ACE; break;
		}
		
		if (r == INVALID) {
			return r;
		}
		
		switch (suit) {
			case 'H': r = toIndex(r, Suit.HEART); break;
			case 'D': r = toIndex(r, Suit.DIAMOND); break;
			case 'C': r = toIndex(r, Suit.CLUB); break;
			case 'S': r = toIndex(r, Suit.SPADE); break;
		}
		
		return r;
	}
	
	public static char getRankChar(int rank) {
		char rankChar = '0';
		
		switch (rank) {
			case Rank.TWO: rankChar = '2'; break;
			case Rank.TEN: rankChar = 'T'; break; 
			case Rank.JACK: rankChar = 'J'; break; 
			case Rank.QUEEN: rankChar = 'Q'; break; 
			case Rank.KING: rankChar = 'K'; break; 
			case Rank.ACE: rankChar = 'A'; break;
			default: rankChar = String.valueOf(rank + 3).charAt(0);
		}
		
		return rankChar;
	}
	
	public static String toString(int rank, int suit) {
		String cardString = "";
		
		String rankChar = String.valueOf(getRankChar(rank));
		
		switch (suit) {
			case Suit.HEART: cardString = rankChar + "H"; break;
			case Suit.DIAMOND: cardString = rankChar + "D"; break;
			case Suit.CLUB: cardString = rankChar + "C"; break;
			case Suit.SPADE: cardString = rankChar + "S"; break;
		}
		
		return cardString;
	}
}
