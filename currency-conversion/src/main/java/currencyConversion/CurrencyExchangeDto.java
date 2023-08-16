package currencyConversion;

import java.math.BigDecimal;

public class CurrencyExchangeDto {

	private String from;
	private String to;
	private BigDecimal conversionMultiple;
	private String environment;

	public CurrencyExchangeDto() {

	}

	public CurrencyExchangeDto(String from, String to, BigDecimal conversionMultiple, String environment) {
		super();
		this.from = from;
		this.to = to;
		this.conversionMultiple = conversionMultiple;
		this.environment = environment;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigDecimal getConversionMultiple() {
		return conversionMultiple;
	}

	public void setConversionMultiple(BigDecimal conversionMultiple) {
		this.conversionMultiple = conversionMultiple;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}
}