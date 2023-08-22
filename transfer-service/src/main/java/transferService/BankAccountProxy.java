package transferService;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {
	
	@GetMapping("/bank-account/{email}")
	BankAccountDto getBankAccount(@PathVariable String email); 
	
	@PutMapping("/bank-account/update-balance-transfer/user/{email}/quantity/{quantity}/from/{currency}/add-subtract/{add_subtract}")
	BankAccountDto updateBankAccountAfterTransfer(@PathVariable String email, @PathVariable BigDecimal quantity, @PathVariable String currency,
															@PathVariable Boolean add_subtract);
}	