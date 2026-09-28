package com.banking.BankingApp.repository;

import com.banking.BankingApp.entity.Account;
import com.banking.BankingApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    public Account findByUser(User user);
    public Account findByaccountNumber(long accountNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :number")
    Optional<Account> findForUpdate(@Param("number") long number);
}
