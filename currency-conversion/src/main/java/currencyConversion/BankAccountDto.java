package currencyConversion;

import java.math.BigDecimal;

public class BankAccountDto {

	private String email;
	private BigDecimal rsd_amount;
	private BigDecimal usd_amount;
	private BigDecimal eur_amount;
	private BigDecimal chf_amount;
	private BigDecimal gbp_amount;
	
	public BankAccountDto () {
		super();
	}

	public BankAccountDto(String email, BigDecimal rsd_amount, BigDecimal usd_amount, BigDecimal eur_amount,
			BigDecimal chf_amount, BigDecimal gbp_amount) {
		super();
		this.email = email;
		this.rsd_amount = rsd_amount;
		this.usd_amount = usd_amount;
		this.eur_amount = eur_amount;
		this.chf_amount = chf_amount;
		this.gbp_amount = gbp_amount;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigDecimal getRsd_amount() {
		return rsd_amount;
	}

	public void setRsd_amount(BigDecimal rsd_amount) {
		this.rsd_amount = rsd_amount;
	}

	public BigDecimal getUsd_amount() {
		return usd_amount;
	}

	public void setUsd_amount(BigDecimal usd_amount) {
		this.usd_amount = usd_amount;
	}

	public BigDecimal getEur_amount() {
		return eur_amount;
	}

	public void setEur_amount(BigDecimal eur_amount) {
		this.eur_amount = eur_amount;
	}

	public BigDecimal getChf_amount() {
		return chf_amount;
	}

	public void setChf_amount(BigDecimal chf_amount) {
		this.chf_amount = chf_amount;
	}

	public BigDecimal getGbp_amount() {
		return gbp_amount;
	}

	public void setGbp_amount(BigDecimal gbp_amount) {
		this.gbp_amount = gbp_amount;
	}
}