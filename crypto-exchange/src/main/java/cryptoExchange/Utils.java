package cryptoExchange;

public class Utils {
	
	public static boolean isCurrencyValid(String currency) {
		switch (currency) {
		case "BTC":
				return true;
		case "ETH":	
				return true;
		case "XRP":	
				return true;
		default:
			return false;
		}
	}
}
