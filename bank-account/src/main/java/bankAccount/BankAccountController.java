package bankAccount;

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
				throw new ApplicationException(
	                "bank-account-not-found",
	                String.format("Bank account with email=%s is not found", email),
	                HttpStatus.NOT_FOUND
				);
		}
			
		bankAccount.setEnvironment(port);
			
		return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
	}
}
