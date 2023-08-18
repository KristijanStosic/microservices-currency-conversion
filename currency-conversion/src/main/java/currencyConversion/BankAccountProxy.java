package currencyConversion;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {
	
	@GetMapping("/bank-account/{email}")
	BankAccountDto getBankAccount(@PathVariable String email);
	
	@PutMapping("/bank-account/update-balance/from/{from}/to/{to}/quantity/{quantity}/user/{email}/totalConvertedAmount/{totalConvertedAmount}")
	BankAccountDto updateBankAccountBalance(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, @PathVariable String email, @PathVariable BigDecimal totalConvertedAmount);
}
