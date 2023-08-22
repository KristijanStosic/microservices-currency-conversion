package tradeService;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.servlet.http.HttpServletRequest;

@FeignClient(name = "currency-conversion")
public interface CurrencyConversionProxy {
	
	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	BankAccountDto getCurrencyConversion(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, @RequestHeader("X-User-Email") String email);
}