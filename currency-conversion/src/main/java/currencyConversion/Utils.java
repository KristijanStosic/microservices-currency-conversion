package currencyConversion;

import java.math.BigDecimal;

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
	
	public static boolean isQuantityValid(BigDecimal quantity) {
		if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
			return false;
		} else {
			return true;
		}
	}
}
