package bankAccount;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class BankAccountController {
	@Autowired
	private Environment environment;

	@Autowired
	private BankAccountRepository bankAccountRepository;

	@Autowired
	private UserProxy userProxy;

	SecurityContext securityContext = SecurityContextHolder.getContext();

	// Get the Auth object
	Authentication authentication = securityContext.getAuthentication();

	@GetMapping("/bank-account/{email}")
	public ResponseEntity<BankAccount> getBankAccount(@PathVariable("email") String email) {
		String port = environment.getProperty("local.server.port");

		BankAccount bankAccount = bankAccountRepository.findByEmail(email);

		if (bankAccount == null) {
			throw new ApplicationException(String.format("Bank account with email=%s is not found", email), HttpStatus.NOT_FOUND);
		}

		bankAccount.setEnvironment(port);

		return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
	}

	@PostMapping("/bank-account/create")
	public ResponseEntity<BankAccount> createBankAccount(@RequestBody BankAccount bankAccount,
			HttpServletRequest request) {

			String port = environment.getProperty("local.server.port");

			BankAccount existingBankAccount = bankAccountRepository.findByEmail(bankAccount.getEmail());

			if (existingBankAccount != null) {
				throw new ApplicationException(
						String.format("Bank account with email=%s already exists", bankAccount.getEmail()),
						HttpStatus.CONFLICT);
			}

			callOtherService(bankAccount.getEmail());

			bankAccount.setEnvironment(port);

			BankAccount createdBankAccount = bankAccountRepository.save(bankAccount);

			return ResponseEntity.status(HttpStatus.CREATED).body(createdBankAccount);
	}
	
	@PutMapping("/bank-account/update/{email}")
	public ResponseEntity<BankAccount> updateBankAccountEntity(@RequestBody BankAccount updatedBankAccount, @PathVariable String email) {
			String port = environment.getProperty("local.server.port");
			
			BankAccount existingBankAccount = bankAccountRepository.findByEmail(email);
			
			if(existingBankAccount == null) {
				throw new ApplicationException(
						String.format("Bank account with email=%s is not found", email), 
						HttpStatus.NOT_FOUND);
			}
			
			if (updatedBankAccount.getChf_amount().equals("") || 
					updatedBankAccount.getEur_amount().equals("") ||
					updatedBankAccount.getRsd_amount().equals("") ||
					updatedBankAccount.getGbp_amount().equals("") ||
					updatedBankAccount.getUsd_amount().equals("")) {
				throw new ApplicationException(
						"Either of the currency values cannot be empty", 
						HttpStatus.BAD_REQUEST);
			}
			
			existingBankAccount.setEmail(existingBankAccount.getEmail());
			existingBankAccount.setChf_amount(updatedBankAccount.getChf_amount());
			existingBankAccount.setEur_amount(updatedBankAccount.getEur_amount());
			existingBankAccount.setGbp_amount(updatedBankAccount.getGbp_amount());
			existingBankAccount.setRsd_amount(updatedBankAccount.getRsd_amount());
			existingBankAccount.setUsd_amount(updatedBankAccount.getUsd_amount());
			existingBankAccount.setEnvironment(port);
			
			bankAccountRepository.save(existingBankAccount);
			return ResponseEntity.status(HttpStatus.OK).body(existingBankAccount);
	}
	
	@PutMapping("/bank-account/update-email/{oldEmail}/for/{newEmail}")
	public ResponseEntity<BankAccount> updateBankAccountEmail(@PathVariable String oldEmail, @PathVariable String newEmail) {

			String port = environment.getProperty("local.server.port");
			
			BankAccount existingBankAccount = bankAccountRepository.findByEmail(oldEmail);

			if(existingBankAccount == null) {
				throw new ApplicationException(
						String.format("Bank account with email=%s is not found", oldEmail), 
						HttpStatus.NOT_FOUND);
			}

			existingBankAccount.setEmail(newEmail);
			existingBankAccount.setEnvironment(port);

			bankAccountRepository.save(existingBankAccount);
			
			return ResponseEntity.status(HttpStatus.OK).body(existingBankAccount);	

	}
	
	@PutMapping("/bank-account/update-balance/from/{from}/to/{to}/quantity/{quantity}/user/{email}/totalConvertedAmount/{totalConvertedAmount}")
	public ResponseEntity<BankAccount> updateBankAccountBalance(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, @PathVariable String email, @PathVariable BigDecimal totalConvertedAmount) {
		BankAccount bankAccount = bankAccountRepository.findByEmail(email);
		
		if (bankAccount == null) {
			throw new ApplicationException(
					String.format("Bank account with email=%s not found", email),
					HttpStatus.NOT_FOUND);
		}
		
		if (Utils.checkBankAccountBalance(from, quantity, bankAccount) ) {
			Utils.subtractBalance(from, quantity, bankAccount);
			Utils.addBalance(to, totalConvertedAmount, bankAccount);
		}
		
		bankAccountRepository.save(bankAccount);
		return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
	}
	
	@DeleteMapping("/bank-account/delete/{email}")
	public ResponseEntity<String> deleteBankAccount(@PathVariable("email") String email) {
		BankAccount existingBankAccount = bankAccountRepository.findByEmail(email);
		
		if (existingBankAccount == null) {
			throw new ApplicationException(
					String.format("Bank account with email=%s not found", email),
					HttpStatus.NOT_FOUND);
		}
		
		bankAccountRepository.delete(existingBankAccount);
		
		return ResponseEntity.status(HttpStatus.OK).body("Bank account deleted successfully.");
	}

	public ResponseEntity<UserDto> callOtherService(String email) {
		try {
			UserDto responseUser = userProxy.getUserByEmail(email);		

			if (!responseUser.getRole().equals(Role.USER)) {
				throw new ApplicationException(
						"Only users with ROLE_USER can have bank account!", 
						HttpStatus.BAD_REQUEST);
			}

			return ResponseEntity.status(HttpStatus.OK).body(responseUser);
		}  catch (FeignException ex) {
			throw new ApplicationException(ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}
}
