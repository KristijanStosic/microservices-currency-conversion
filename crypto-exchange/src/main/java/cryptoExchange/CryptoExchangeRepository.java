package cryptoExchange;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptoExchangeRepository extends JpaRepository<CryptoExchange, Long>{

	CryptoExchange findByFromAndToContainingIgnoreCase(String from, String to);
}
