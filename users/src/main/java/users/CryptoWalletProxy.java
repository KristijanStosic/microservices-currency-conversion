package users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {
	
	@PutMapping("/crypto-wallet/update-email/{oldEmail}/for/{newEmail}")
	public ResponseEntity<CryptoWalletDto> updateCryptoWalletEmail(@PathVariable("oldEmail") String oldEmail, @PathVariable("newEmail") String newEmail);
	
	@DeleteMapping("/crypto-wallet/delete/{email}")
	public ResponseEntity<String> deleteCryptoWallet(@PathVariable String email);
}
