package cryptoExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cryptoExchange.ApplicationException;
import cryptoExchange.CryptoExchange;
import cryptoExchange.Utils;

@RestController
public class CryptoExchangeController {

	@Autowired
	private CryptoExchangeRepository cryptoExchangeRepository;
	
	@Autowired 
	private Environment environment;
	
	@GetMapping("/crypto-exchange/from/{from}/to/{to}")
	public ResponseEntity<CryptoExchange> getExchange(@PathVariable String from, @PathVariable String to) {
		String port = environment.getProperty("local.server.port");
		CryptoExchange cryptoExchange = cryptoExchangeRepository.findByFromAndToContainingIgnoreCase(from, to);
		
		if(!Utils.isCurrencyValid(from) || !Utils.isCurrencyValid(to) || from.equals(to)) {
			throw new ApplicationException(
	                "requested-query-parameters-from-and-to-are-invalid",
	                "Query parameters from and to are invalid. Enter them correctly",
	                HttpStatus.BAD_REQUEST
				);
		}
		
		if (cryptoExchange == null) {
			throw new ApplicationException(
	                "requested-crypto-exchange-could-not-be-found",
	                "Requested crypto exchange could not be found!",
	                HttpStatus.NOT_FOUND
				);
			
		}
		
		cryptoExchange.setEnvironment(port);
		CryptoExchange exchange = new CryptoExchange(cryptoExchange.getId(), from, to, cryptoExchange.getConversionMultiple(), port);
		return ResponseEntity.status(HttpStatus.OK).body(exchange);
	}
}
