package bankAccount;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	BankAccount findByEmail(String email);
}
