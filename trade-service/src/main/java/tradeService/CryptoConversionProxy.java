package tradeService;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "crypto-conversion")
public interface CryptoConversionProxy {
	
	@GetMapping("/crypto-conversion/from/{from}/to/{to}/quantity/{quantity}/user/{email}")
	CryptoWalletDto getCryptoConversion(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, @PathVariable String email);
}