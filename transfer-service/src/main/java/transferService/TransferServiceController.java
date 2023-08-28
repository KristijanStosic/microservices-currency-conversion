package transferService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class TransferServiceController {
	
	@Autowired
    private BankAccountProxy bankAccountProxy;

    @Autowired
    private CryptoWalletProxy cryptoWalletProxy;


    @GetMapping("/transfer-service/currency/{currency}/amount/{amount}/to/user/{email}")
    //@CircuitBreaker(name = "transferService", fallbackMethod = "getFallback")
   //@RateLimiter(name = "transferService", fallbackMethod = "getFallbackResponse")
    public ResponseEntity<?> getTransfer(@PathVariable String currency, @PathVariable BigDecimal amount, @PathVariable String email, HttpServletRequest request) {
            String moneySender = request.getHeader("X-User-Email");

            BigDecimal amountWithProvision = amount.add(amount.multiply(BigDecimal.valueOf(0.01)));
            
            if (!Utils.isValidCurrency(currency)) {
            	throw new ApplicationException("Not valid currency parameter", HttpStatus.BAD_REQUEST);
            }

            if(currency.equals("BTC") || currency.equals("ETH") || currency.equals("XRP")){
            	
                CryptoWalletDto sender = cryptoWalletProxy.getCryptoWallet(moneySender);
                CryptoWalletDto receiver = cryptoWalletProxy.getCryptoWallet(email);
                
                if (sender == null || receiver == null) {
                    throw new ApplicationException("Crypto wallet of user doesnt exist!", HttpStatus.NOT_FOUND);
                }
                
                BigDecimal amountAvailable = Utils.getCryptoAmount(currency, sender);
                
                if (amountAvailable.compareTo(amountWithProvision) >= 0) {
                	
                    cryptoWalletProxy.updateCryptoWalletAfterTransfer(moneySender, amountWithProvision, currency, false);
                    cryptoWalletProxy.updateCryptoWalletAfterTransfer(email, amount, currency, true);
                         
                    return ResponseEntity.ok().body(
                            "User : " + moneySender +
                                    " has successfully transfered " + amount + " " + currency +
                                    " to user " + email);
                } else {
                    throw new ApplicationException("Amount of " + currency + " user has in his crypto wallet is less than wanted quantity: " + amount, HttpStatus.BAD_REQUEST);
                }
            }
            
            if(currency.equals("EUR") || currency.equals("USD") || currency.equals("RSD") || currency.equals("GBP") || currency.equals("CHF")) {
            	
                BankAccountDto sender = bankAccountProxy.getBankAccount(moneySender);
                BankAccountDto receiver = bankAccountProxy.getBankAccount(email);
                
                if(sender == null || receiver == null){
                	throw new ApplicationException("Bank account does not exist!", HttpStatus.NOT_FOUND);
                }
                
                BigDecimal amountAvailable = Utils.getFiatAmount(currency, sender);
                
                if(amountAvailable.compareTo(amountWithProvision) >= 0) {
                	
                    bankAccountProxy.updateBankAccountAfterTransfer(moneySender, amountWithProvision, currency, false);
                    bankAccountProxy.updateBankAccountAfterTransfer(email, amount, currency, true);
                    
                    return ResponseEntity.ok().body(
                            "User : " + moneySender +
                                    " has successfully transfered " + amount + " " + currency +
                                    " to user " + email);
                } else {
                	 throw new ApplicationException("Amount of " + currency + " user has in his bank account is less than wanted quantity: " + amount, HttpStatus.BAD_REQUEST);
                }
            }
            return null;
    }
    
    public String getFallback(Exception e) {
    	return "Service unavailable !";
    }
    
    public String getFallbackResponse(Exception e) {
    	return "You can only send 2 requests within 30 seconds!";
    }
}
