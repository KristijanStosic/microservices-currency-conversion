package cryptoConversion;

import java.math.BigDecimal;

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
	
	public static boolean isQuantityValid(BigDecimal quantity) {
		if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
			return false;
		} else {
			return true;
		}
	}
}
