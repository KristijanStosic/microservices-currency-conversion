package bankAccount;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

public class Utils {
	
	public static boolean checkBankAccountBalance(String currency, BigDecimal quantity, BankAccount bankAccount) {
		switch (currency) {
		case "EUR":
			if(bankAccount.getEur_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough EUR on your bank account", HttpStatus.BAD_REQUEST);
		case "USD":	
			if(bankAccount.getUsd_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough USD on your bank account", HttpStatus.BAD_REQUEST);
		case "GBP":	
			if(bankAccount.getGbp_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough GBP on your bank account", HttpStatus.BAD_REQUEST);
		case "CHF":	
			if(bankAccount.getChf_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough CHF on your bank account", HttpStatus.BAD_REQUEST);
		case "RSD":	
			if(bankAccount.getRsd_amount().compareTo(quantity) >= 0) {
				return true;
			}
			throw new ApplicationException("Not enough RSD on your bank account", HttpStatus.BAD_REQUEST);
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
	
	public static void addBalance(String currency, BigDecimal quantity, BankAccount bankAccount) {
		switch (currency) {
		case "EUR":
			bankAccount.setEur_amount(bankAccount.getEur_amount().add(quantity));
			break;
		case "USD":
			bankAccount.setUsd_amount(bankAccount.getUsd_amount().add(quantity));
			break;
		case "RSD":
			bankAccount.setRsd_amount(bankAccount.getRsd_amount().add(quantity));
		break;
		case "GBP":
			bankAccount.setGbp_amount(bankAccount.getGbp_amount().add(quantity));
		break;
		case "CHF":
			bankAccount.setChf_amount(bankAccount.getChf_amount().add(quantity));
		break;
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
	
	public static void subtractBalance(String currency, BigDecimal quantity, BankAccount bankAccount) {
		switch (currency) {
		case "EUR":
			bankAccount.setEur_amount(bankAccount.getEur_amount().subtract(quantity));
			break;
		case "USD":
			bankAccount.setUsd_amount(bankAccount.getUsd_amount().subtract(quantity));
			break;
		case "RSD":
			bankAccount.setRsd_amount(bankAccount.getRsd_amount().subtract(quantity));
		break;
		case "GBP":
			bankAccount.setGbp_amount(bankAccount.getGbp_amount().subtract(quantity));
		break;
		case "CHF":
			bankAccount.setChf_amount(bankAccount.getChf_amount().subtract(quantity));
		break;
		default:
			throw new ApplicationException("Incorrect currency FROM or TO parameter", HttpStatus.BAD_REQUEST);
		}
	}
}
