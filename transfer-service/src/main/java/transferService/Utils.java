package transferService;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

public class Utils {
	
	public static BigDecimal getCryptoAmount(String currency, CryptoWalletDto cryptoWallet) {
        switch (currency) {
            case "BTC":
                return cryptoWallet.getBtc_amount();
            case "ETH":
                return cryptoWallet.getEth_amount();
            case "XRP":
                return cryptoWallet.getXrp_amount();
            default:
                return BigDecimal.ZERO;
        }
    }

    public static BigDecimal getFiatAmount(String currency, BankAccountDto bankAccount) {
        switch (currency) {
            case "EUR":
                return bankAccount.getEur_amount();
            case "USD":
                return bankAccount.getUsd_amount();
            case "RSD":
                return bankAccount.getRsd_amount();
            case "GBP":
                return bankAccount.getGbp_amount();
            case "CHF":
            	return bankAccount.getChf_amount();
            default:
            	return BigDecimal.ZERO;
        }
    }
    
    public static boolean isValidCurrency(String currency) {
    	switch (currency) {
		case "RSD": 
			return true;
		case "USD": 
			return true;
		case "EUR": 
			return true;
		case "GBP": 
			return true;
		case "CHF": 
			return true;
		case "BTC": 
			return true;
		case "ETH": 
			return true;
		case "XRP": 
			return true;
		default:
			throw new ApplicationException("Unexpected value: " + currency, HttpStatus.BAD_REQUEST);
		}
    }
}
