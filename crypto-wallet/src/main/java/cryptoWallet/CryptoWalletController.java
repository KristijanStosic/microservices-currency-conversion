package cryptoWallet;

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
public class CryptoWalletController {
	@Autowired
	private Environment environment;

	@Autowired
	private CryptoWalletRepository cryptoWalletRepository;

	@Autowired
	private UserProxy userProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;

	SecurityContext securityContext = SecurityContextHolder.getContext();

	// Get the Auth object
	Authentication authentication = securityContext.getAuthentication();

	@GetMapping("/crypto-wallet/{email}")
	public ResponseEntity<CryptoWallet> getCryptoWallet(@PathVariable("email") String email) {
		String port = environment.getProperty("local.server.port");

		CryptoWallet cryptoWallet = cryptoWalletRepository.findByEmail(email);

		if (cryptoWallet == null) {
			throw new ApplicationException(String.format("Crypto wallet with email=%s is not found", email), HttpStatus.NOT_FOUND);
		}

		cryptoWallet.setEnvironment(port);

		return ResponseEntity.status(HttpStatus.OK).body(cryptoWallet);
	}

	@PostMapping("/crypto-wallet/create")
	public ResponseEntity<CryptoWallet> createCryptoWallet(@RequestBody CryptoWallet cryptoWallet, HttpServletRequest request) {

			String port = environment.getProperty("local.server.port");

			CryptoWallet existingCryptoWallet = cryptoWalletRepository.findByEmail(cryptoWallet.getEmail());

			if (existingCryptoWallet != null) {
				throw new ApplicationException(
						String.format("Crypto wallet with email=%s already exists", cryptoWallet.getEmail()),
						HttpStatus.CONFLICT);
			}

			callOtherService(cryptoWallet.getEmail());

			cryptoWallet.setEnvironment(port);

			CryptoWallet createdCryptoWallet = cryptoWalletRepository.save(cryptoWallet);

			return ResponseEntity.status(HttpStatus.CREATED).body(createdCryptoWallet);
	}
	
	@PutMapping("/crypto-wallet/update/{email}")
	public ResponseEntity<CryptoWallet> updateCryptoWalletEntity(@RequestBody CryptoWallet updatedCryptoWallet, @PathVariable String email) {
			String port = environment.getProperty("local.server.port");
			
			CryptoWallet existingCryptoWallet = cryptoWalletRepository.findByEmail(email);
			
			if(existingCryptoWallet == null) {
				throw new ApplicationException(
						String.format("Crypto wallet with email=%s is not found", email), 
						HttpStatus.NOT_FOUND);
			}
			
			if (updatedCryptoWallet.getBtc_amount().equals("") || 
					updatedCryptoWallet.getEth_amount().equals("") ||
					updatedCryptoWallet.getXrp_amount().equals("")) {
				throw new ApplicationException(
						"Either of the currency values cannot be empty", 
						HttpStatus.BAD_REQUEST);
			}
			
			existingCryptoWallet.setEmail(existingCryptoWallet.getEmail());
			existingCryptoWallet.setBtc_amount(updatedCryptoWallet.getBtc_amount());
			existingCryptoWallet.setEth_amount(updatedCryptoWallet.getEth_amount());
			existingCryptoWallet.setXrp_amount(updatedCryptoWallet.getXrp_amount());
			existingCryptoWallet.setEnvironment(port);
			
			cryptoWalletRepository.save(existingCryptoWallet);
			return ResponseEntity.status(HttpStatus.OK).body(existingCryptoWallet);
	}
	
	@PutMapping("/crypto-wallet/update-email/{oldEmail}/for/{newEmail}")
	public ResponseEntity<CryptoWallet> updateCryptoWalletEmail(@PathVariable String oldEmail, @PathVariable String newEmail) {

			String port = environment.getProperty("local.server.port");
			
			CryptoWallet existingCryptoWallet = cryptoWalletRepository.findByEmail(oldEmail);

			if(existingCryptoWallet == null) {
				throw new ApplicationException(
						String.format("Crypto wallet with email=%s is not found", oldEmail), 
						HttpStatus.NOT_FOUND);
			}

			existingCryptoWallet.setEmail(newEmail);
			existingCryptoWallet.setEnvironment(port);

			cryptoWalletRepository.save(existingCryptoWallet);
			
			return ResponseEntity.status(HttpStatus.OK).body(existingCryptoWallet);	
	}
	
	@PutMapping("/crypto-wallet/update-balance/from/{from}/to/{to}/quantity/{quantity}/user/{email}/totalConvertedAmount/{totalConvertedAmount}")
	public ResponseEntity<CryptoWallet> updateBankAccountBalance(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, @PathVariable String email, @PathVariable BigDecimal totalConvertedAmount) {
		CryptoWallet cryptoWallet = cryptoWalletRepository.findByEmail(email);
		
		if (cryptoWallet == null) {
			throw new ApplicationException(
					String.format("Crypto wallet with email=%s not found", email),
					HttpStatus.NOT_FOUND);
		}
		
		if (Utils.checkBankAccountBalance(from, quantity, cryptoWallet) ) {
			Utils.subtractBalance(from, quantity, cryptoWallet);
			Utils.addBalance(to, totalConvertedAmount, cryptoWallet);
		}
		
		cryptoWalletRepository.save(cryptoWallet);
		return ResponseEntity.status(HttpStatus.OK).body(cryptoWallet);
	}
	
	@PutMapping("/crypto-wallet/update-balance-trade/from/{from}/to/{to}/quantity/{quantity}/user/{email}")
	public CryptoWallet updateCryptoWalletAfterTrade(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity, @PathVariable String email) {
		CryptoWallet cryptoWallet = cryptoWalletRepository.findByEmail(email);
		
		if (from.equals("BTC") || from.equals("ETH") || from.equals("XRP")) {
			if (Utils.checkBankAccountBalance(from, quantity, cryptoWallet)) {
				Utils.subtractBalance(from, quantity, cryptoWallet);
			}
		}

		if (to.equals("BTC") || to.equals("ETH") || to.equals("XRP")) {
			Utils.addBalance(to, quantity, cryptoWallet);
		}

		return cryptoWalletRepository.save(cryptoWallet);
	}
	
	@PutMapping("/crypto-wallet/update-balance-transfer/user/{email}/quantity/{quantity}/from/{currency}/add-subtract/{add_subtract}")
    public ResponseEntity<CryptoWallet> updateCryptoWalletAfterTransfer(@PathVariable String email, @PathVariable BigDecimal quantity,
                                                                  @PathVariable String currency, @PathVariable Boolean add_subtract) {
		
            CryptoWallet cryptoWallet = cryptoWalletRepository.findByEmail(email);

            if(cryptoWallet == null){
                throw new ApplicationException("There is no crypto wallet for user with email " + email, HttpStatus.NOT_FOUND);
            }

            if(add_subtract) {
                Utils.addBalance(currency, quantity, cryptoWallet);
            } else {
                Utils.subtractBalance(currency, quantity, cryptoWallet);
            }

            cryptoWalletRepository.save(cryptoWallet);
            return ResponseEntity.status(HttpStatus.OK).body(cryptoWallet);	
    }
	
	@DeleteMapping("/crypto-wallet/delete/{email}")
	public ResponseEntity<String> deleteCryptoWallet(@PathVariable("email") String email) {
		CryptoWallet existingCryptoWallet = cryptoWalletRepository.findByEmail(email);
		
		if (existingCryptoWallet == null) {
			throw new ApplicationException(
					String.format("Crypto wallet with email=%s not found", email),
					HttpStatus.NOT_FOUND);
		}
		
		cryptoWalletRepository.delete(existingCryptoWallet);
		
		return ResponseEntity.status(HttpStatus.OK).body("Crypto wallet deleted successfully.");
	}

	public ResponseEntity<UserDto> callOtherService(String email) {
		try {
			UserDto responseUser = userProxy.getUserByEmail(email);	
			
			BankAccountDto responseBankAccount = bankAccountProxy.getBankAccount(email);
				

			if (!responseUser.getRole().equals(Role.USER)) {
				throw new ApplicationException(
						"Only users with ROLE_USER can have crypto wallet!", 
						HttpStatus.BAD_REQUEST);
			}
			
			if (responseBankAccount == null) {
				throw new ApplicationException(
						"There is no crypto WALLET with this email!", 
						HttpStatus.BAD_REQUEST);
			}

			return ResponseEntity.status(HttpStatus.OK).body(responseUser);
		}  catch (FeignException ex) {
			throw new ApplicationException(ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}
}
