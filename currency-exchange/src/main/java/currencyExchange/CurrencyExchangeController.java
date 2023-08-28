package currencyExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CurrencyExchangeController {
	
	@Autowired
	private CurrencyExchangeRepository currencyExchangeRepository;
	
	@Autowired 
	private Environment environment;

	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	public ResponseEntity<CurrencyExchange> getExchange(@PathVariable String from, @PathVariable String to) {
		String port = environment.getProperty("local.server.port");
		CurrencyExchange currencyExchange = currencyExchangeRepository.findByFromAndToContainingIgnoreCase(from, to);
		
		if(!Utils.isCurrencyValid(from) || !Utils.isCurrencyValid(to) || from.equals(to)) {
			throw new ApplicationException(
	                "requested-query-parameters-from-and-to-are-invalid",
	                "Query parameters from and to are invalid. Enter them correctly",
	                HttpStatus.BAD_REQUEST
				);
		}
		
		if (currencyExchange == null) {
			throw new ApplicationException(
	                "requested-currency-exchange-could-not-be-found",
	                "Requested currency exchange could not be found!",
	                HttpStatus.NOT_FOUND
				);
			
		}
		
		currencyExchange.setEnvironment(port);
		CurrencyExchange exchange = new CurrencyExchange(currencyExchange.getId(), from, to, currencyExchange.getConversionMultiple(), port);
		return ResponseEntity.status(HttpStatus.OK).body(exchange);
	}
}
