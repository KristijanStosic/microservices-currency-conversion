package users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {
	@PutMapping("/bank-account/update/{oldEmail}/for/{newEmail}")
	public ResponseEntity<BankAccountDto> updateBankAccountEmail(@PathVariable("oldEmail") String oldEmail, @PathVariable("newEmail") String newEmail);
	
	@DeleteMapping("/bank-account/delete/{email}")
	public ResponseEntity<String> deleteBankAccount(@PathVariable String email);
}
