package com.banking.BankingApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.banking.BankingApp.service.TransactionService;
import com.banking.BankingApp.repository.AccountRepository;
import com.banking.BankingApp.security.JwtUtils;
import com.banking.BankingApp.exception.CustomException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:astertest;DB_CLOSE_DELAY=-1")
class BankingAppApplicationTests {
	@Autowired TransactionService transactions;
	@Autowired AccountRepository accounts;
	@Autowired JwtUtils jwt;

	@Test
	void contextLoads() {
	}

	@Test
	void transferMovesFundsOnceAndRejectsInvalidAmounts() {
		String senderToken = jwt.generateToken("alex@aster.demo");
		long sender = 4100200011L;
		long receiver = 4100200012L;
		double senderBefore = accounts.findByaccountNumber(sender).getBalance();
		double receiverBefore = accounts.findByaccountNumber(receiver).getBalance();

		assertThrows(CustomException.class, () -> transactions.transfer(senderToken, -10, receiver));
		assertThrows(CustomException.class, () -> transactions.transfer(senderToken, senderBefore + 1, receiver));
		assertThrows(CustomException.class, () -> transactions.transfer(senderToken, 10, 9999999999L));
		assertEquals(senderBefore, accounts.findByaccountNumber(sender).getBalance(), 0.001);

		transactions.transfer(senderToken, 125.50, receiver);
		assertEquals(senderBefore - 125.50, accounts.findByaccountNumber(sender).getBalance(), 0.001);
		assertEquals(receiverBefore + 125.50, accounts.findByaccountNumber(receiver).getBalance(), 0.001);
	}

}
