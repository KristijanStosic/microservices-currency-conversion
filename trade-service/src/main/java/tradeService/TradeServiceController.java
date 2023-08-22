package tradeService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class TradeServiceController {
	
	@Autowired
	private TradeServiceRepository tradeServiceRepository;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;

	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;

	@Autowired
	private CurrencyConversionProxy currencyConversionProxy;
	
	@Autowired
	private CurrencyExchangeProxy currencyExchangeProxy;
	
	SecurityContext securityContext = SecurityContextHolder.getContext();

	// Get the Auth object
	Authentication authentication = securityContext.getAuthentication();
	
	@GetMapping("/trade-service/from/{from}/to/{to}/quantity/{quantity}")
	public ResponseEntity<?> getTrade(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, HttpServletRequest request) {
		String role = request.getHeader("X-User-Role");
		String email = request.getHeader("X-User-Email");
		
		if (!Utils.isCurrencyValid(from) && !Utils.isCryptoCurrencyValid(from)) {
			throw new ApplicationException("Not valid currency in FROM parameter", HttpStatus.BAD_REQUEST);
		}

		if (!Utils.isCurrencyValid(to) && !Utils.isCryptoCurrencyValid(to)) {
			throw new ApplicationException("Not valid currency in TO parameter", HttpStatus.BAD_REQUEST);
		}
	
		if ((from.toUpperCase().equals(to))) {
			throw new ApplicationException("Trade must be done between different currencies", HttpStatus.BAD_REQUEST);
		}
		
		if (Utils.isCurrencyValid(from) && Utils.isCurrencyValid(to)) {
			throw new ApplicationException("Trade must be done between crypto and fiat currency", HttpStatus.BAD_REQUEST);
		}
		
		if (Utils.isCryptoCurrencyValid(from) && Utils.isCryptoCurrencyValid(to)) {
			throw new ApplicationException("Trade must be done between crypto and fiat currency", HttpStatus.BAD_REQUEST);
		}

		//bankAccountProxy.getBankAccount(email);
		//cryptoWalletProxy.getCryptoWallet(email);

		TradeService trade = tradeServiceRepository.findByFromAndToContainingIgnoreCase(from, to);

		CryptoWalletDto cryptoWallet;
		BankAccountDto bankAccount;
		CurrencyExchangeDto currencyExchange;
		
		bankAccount = bankAccountProxy.getBankAccount(email);
		cryptoWallet = cryptoWalletProxy.getCryptoWallet(email);
		
		BigDecimal oldAmount, newAmount, difference;
		
		if (role != null) {
			if(from.equals("USD") || from.equals("EUR")) {	
				if(to.equals("BTC") || to.equals("ETH") || to.equals("XRP")) {
					bankAccountProxy.updateBankAccountAfterTrade(from, to, quantity, email);
					cryptoWallet = cryptoWalletProxy.updateCryptoWalletAfterTrade(from, to, quantity.multiply(trade.getConversionMultiple()), email);
				}
				cryptoWallet.setMessage("Trade is successfully done." + " Converted: " + " " + quantity + " " + from + " for " + to
						+ ". Total converted amount: " + quantity.multiply(trade.getConversionMultiple()));
				return ResponseEntity.ok().body(cryptoWallet);
			} 
			
			
			if (from.equals("RSD") || from.equals("CHF") || from.equals("GBP")) {
				if (to.equals("BTC") || to.equals("ETH") || to.equals("XRP")) {
					trade = tradeServiceRepository.findByFromAndToContainingIgnoreCase("USD", to);				
					
					oldAmount = bankAccount.getUsd_amount(); 

					currencyConversionProxy.getCurrencyConversion(from, "USD", quantity, email);
					
					newAmount = bankAccountProxy.getBankAccount(email).getUsd_amount();
					
					difference = newAmount.subtract(oldAmount);
					
					bankAccountProxy.updateBankAccountAfterTrade("USD", to, difference, email);
					
					cryptoWallet = cryptoWalletProxy.updateCryptoWalletAfterTrade("USD", to, difference.multiply(trade.getConversionMultiple()), email);

					
					cryptoWallet.setMessage("Trade is successfully done." + " Converted: " + " " + quantity + " " + from + " for " + to
							+ ". Total converted amount: " + quantity.multiply(trade.getConversionMultiple()));
					
					return ResponseEntity.ok().body(cryptoWallet);
				}
			}

			
			if(from.equals("BTC") || from.equals("ETH") || from.equals("XRP")) {	
				if(to.equals("EUR") || to.equals("USD")) {
					cryptoWalletProxy.updateCryptoWalletAfterTrade(from, to, quantity, email);
					bankAccount = bankAccountProxy.updateBankAccountAfterTrade(from, to, quantity.multiply(trade.getConversionMultiple()), email);
					
					bankAccount.setMessage("Trade is successfully done." + " Converted: " + " " + quantity + " " + from + " for " + to
							+ ". Total converted amount: " + quantity.multiply(trade.getConversionMultiple()));
					return ResponseEntity.ok().body(bankAccount);
				}
				
				if(to.equals("RSD") || to.equals("GBP") || to.equals("CHF")) {
					trade = tradeServiceRepository.findByFromAndToContainingIgnoreCase(from, "USD");
					
					oldAmount = bankAccount.getUsd_amount();
					
					bankAccount = bankAccountProxy.updateBankAccountAfterTrade(from, "USD", quantity.multiply(trade.getConversionMultiple()), email);
					
					currencyConversionProxy.getCurrencyConversion("USD", "RSD", quantity, email);
					
					newAmount = bankAccountProxy.getBankAccount(email).getUsd_amount();
					
					difference = newAmount.subtract(oldAmount);		
					
					currencyExchange = currencyExchangeProxy.getExchange("USD", to);
					
					cryptoWalletProxy.updateCryptoWalletAfterTrade(from, to, quantity, email);
					
					bankAccountProxy.updateBankAccountAfterTrade("USD", "BTC", difference, email);
					
					bankAccount = bankAccountProxy.updateBankAccountAfterTrade(from, to, difference.multiply(currencyExchange.getConversionMultiple()), email);
					
					bankAccount.setMessage("Trade is successfully done." + " Converted: " + " " + quantity + " " + from + " for " + to
							+ ". Total converted amount: " + quantity.multiply(trade.getConversionMultiple()));
				}	
			}
		}
		return ResponseEntity.ok().body(bankAccount);
	}
}
