package cryptoWallet;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

public class Utils {
	
	public static boolean checkBankAccountBalance(String currency, BigDecimal quantity, CryptoWallet cryptoWallet) {
		switch (currency) {
		case "BTC":
			if(cryptoWallet.getBtc_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough BTC on your bank account", HttpStatus.BAD_REQUEST);
		case "ETH":	
			if(cryptoWallet.getEth_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough ETH on your bank account", HttpStatus.BAD_REQUEST);
		case "XRP":	
			if(cryptoWallet.getXrp_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough XRP on your bank account", HttpStatus.BAD_REQUEST);
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
	
	public static void addBalance(String currency, BigDecimal quantity, CryptoWallet cryptoWallet) {
		switch (currency) {
		case "BTC":
			cryptoWallet.setBtc_amount(cryptoWallet.getBtc_amount().add(quantity));
			break;
		case "ETH":
			cryptoWallet.setEth_amount(cryptoWallet.getEth_amount().add(quantity));
			break;
		case "XRP":
			cryptoWallet.setXrp_amount(cryptoWallet.getXrp_amount().add(quantity));
		break;
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
	
	public static void subtractBalance(String currency, BigDecimal quantity, CryptoWallet cryptoWallet) {
		switch (currency) {
		case "BTC":
			cryptoWallet.setBtc_amount(cryptoWallet.getBtc_amount().subtract(quantity));
			break;
		case "ETH":
			cryptoWallet.setEth_amount(cryptoWallet.getEth_amount().subtract(quantity));
			break;
		case "XRP":
			cryptoWallet.setXrp_amount(cryptoWallet.getXrp_amount().subtract(quantity));
		break;
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
}
