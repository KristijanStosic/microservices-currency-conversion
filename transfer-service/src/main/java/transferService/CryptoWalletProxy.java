package transferService;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {

	@GetMapping("/crypto-wallet/{email}")
	CryptoWalletDto getCryptoWallet(@PathVariable String email); 
	
	@PutMapping("/crypto-wallet/update-balance-transfer/user/{email}/quantity/{quantity}/from/{currency}/add-subtract/{add_subtract}")
	BankAccountDto updateCryptoWalletAfterTransfer(@PathVariable String email, @PathVariable BigDecimal quantity, @PathVariable String currency,
															@PathVariable Boolean add_subtract);
}