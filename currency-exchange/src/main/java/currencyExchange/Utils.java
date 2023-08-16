package currencyExchange;

public class Utils {
	
	public static boolean isCurrencyValid(String currency) {
		switch (currency) {
		case "EUR":
				return true;
		case "USD":	
				return true;
		case "GBP":	
				return true;
		case "CHF":	
				return true;
		case "RSD":	
				return true;
		default:
			return false;
		}
	}
}
