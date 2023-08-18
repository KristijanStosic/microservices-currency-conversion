package currencyConversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import feign.FeignException;

@RestController
public class CurrencyConversionController {
	
	@Autowired
	private CurrencyExchangeProxy currencyExchangeProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	//localhost:8100/currency-conversion?from=EUR&to=RSD&quantity=50
	//localhost:8765/currency-conversion/from/EUR/to/RSD/quantity/10
	//@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	@GetMapping("/currency-conversion")
	public ResponseEntity<?> getConversion(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity, @RequestParam String email) {
		
		try {
			if (!Utils.isCurrencyValid(from) || !Utils.isCurrencyValid(to)) {
				throw new ApplicationException(
		                "requested-query-parameters-from-and-to-are-invalid",
		                "Query parameters from and to are invalid. Enter them correctly",
		                HttpStatus.BAD_REQUEST
					);
			}
			
			if (!Utils.isQuantityValid(quantity)) {
				throw new ApplicationException(
		                "requested-query-parameters-quantity-is-invalid",
		                "Query parameter quantity must be greater than 0",
		                HttpStatus.BAD_REQUEST
					);
			}
			
			CurrencyExchangeDto responseCurrencyExchange = currencyExchangeProxy.getExchange(from, to);
			
			/*CurrencyConversion newConversion = new CurrencyConversion(
					from, 
					to, 
					responseCurrencyExchange.getConversionMultiple(),
					responseCurrencyExchange.getEnvironment(),
					responseCurrencyExchange.getConversionMultiple().multiply(quantity),
					quantity);
			
			return ResponseEntity.status(HttpStatus.OK).body(newConversion);*/
			
			return getResponseEntity(from, to, quantity, email);
		} catch(FeignException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		} 
	}
	
	private ResponseEntity<?> getResponseEntity(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity, String email) {
		try {
			
			CurrencyExchangeDto responseCurrencyExchange = currencyExchangeProxy.getExchange(from, to);
			
			CurrencyConversion newConversion = new CurrencyConversion(
					from, 
					to, 
					responseCurrencyExchange.getConversionMultiple(), 
					responseCurrencyExchange.getEnvironment(),
					quantity, 
					responseCurrencyExchange.getConversionMultiple().multiply(quantity));
			
			BankAccountDto updatedBalance = bankAccountProxy.updateBankAccountBalance(from, to, quantity, email, newConversion.getConversionTotal());
			
			updatedBalance.setMessage("Currency conversion success. " + quantity + " " + from + " for " + to);
			
			return ResponseEntity.ok().body(updatedBalance);
		} catch (FeignException ex) {
			throw new ApplicationException("", ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	/*//localhost:8100/currency-conversion/from/EUR/to/RSD/quantity/100
		@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
		public CurrencyConversion getConversion
			(@PathVariable String from, @PathVariable String to, @PathVariable double quantity) {
			
			HashMap<String,String> uriVariables = new HashMap<String,String>();
			uriVariables.put("from", from);
			uriVariables.put("to", to);
			
			ResponseEntity<CurrencyConversion> response = 
					new RestTemplate().
					getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
							CurrencyConversion.class, uriVariables);
			
			CurrencyConversion cc = response.getBody();
			
			return new CurrencyConversion(from,to,cc.getConversionMultiple(), cc.getEnvironment(), quantity,
					cc.getConversionMultiple().multiply(BigDecimal.valueOf(quantity)));
		}
		
		//localhost:8100/currency-conversion?from=EUR&to=RSD&quantity=50
		@GetMapping("/currency-conversion")
		public ResponseEntity<?> getConversionParams(@RequestParam String from, @RequestParam String to, @RequestParam double quantity) {
			
			HashMap<String,String> uriVariable = new HashMap<String, String>();
			uriVariable.put("from", from);
			uriVariable.put("to", to);
			
			try {
			ResponseEntity<CurrencyConversion> response = new RestTemplate().
					getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversion.class, uriVariable);
			CurrencyConversion responseBody = response.getBody();
			return ResponseEntity.status(HttpStatus.OK).body(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment(),
					quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));
			}
			catch(HttpClientErrorException e) {
				return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
			}
	}*/
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
	    String parameter = ex.getParameterName();
	    //return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
	    return ResponseEntity.status(ex.getStatusCode()).body("Value [" + ex.getParameterType() + "] of parameter [" + parameter + "] has been ommited");
	}
	
	
}
