package cryptoWallet;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
	CryptoWallet findByEmail(String email);
}
