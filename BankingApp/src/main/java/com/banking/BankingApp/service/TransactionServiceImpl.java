package com.banking.BankingApp.service;

import com.banking.BankingApp.entity.Account;
import com.banking.BankingApp.entity.Transaction;
import com.banking.BankingApp.enums.TransactionType;
import com.banking.BankingApp.exception.CustomException;
import com.banking.BankingApp.repository.AccountRepository;
import com.banking.BankingApp.security.CustomerUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final CustomerUserDetails userDetails;
    private final AccountRepository accounts;

    public TransactionServiceImpl(CustomerUserDetails userDetails, AccountRepository accounts) {
        this.userDetails = userDetails;
        this.accounts = accounts;
    }

    private double validAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0 || Math.abs(Math.round(amount * 100) - amount * 100) > 0.000001) {
            throw new CustomException("Enter an amount greater than zero with at most two decimal places");
        }
        return amount;
    }

    private Account ownAccount(String jwt) {
        long number = accounts.findByUser(userDetails.getUserFromJwtToken(jwt)).getAccountNumber();
        return accounts.findForUpdate(number).orElseThrow(() -> new CustomException("Account not found"));
    }

    private void record(Account account, double amount, TransactionType type, long counterparty) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCounterParty(counterparty);
        account.getTransactions().add(tx);
    }

    @Override
    @Transactional
    public void addMoney(String jwt, double amount) {
        amount = validAmount(amount);
        Account account = ownAccount(jwt);
        account.setBalance(account.getBalance() + amount);
        record(account, amount, TransactionType.CREDIT, 0);
        accounts.save(account);
    }

    @Override
    @Transactional
    public void debitMoney(String jwt, double amount) {
        amount = validAmount(amount);
        Account account = ownAccount(jwt);
        if (account.getBalance() < amount) throw new CustomException("Insufficient balance");
        account.setBalance(account.getBalance() - amount);
        record(account, amount, TransactionType.DEBIT, 0);
        accounts.save(account);
    }

    @Override
    @Transactional
    public void transfer(String jwt, double amount, long receiverAccountNo) {
        amount = validAmount(amount);
        long senderNumber = accounts.findByUser(userDetails.getUserFromJwtToken(jwt)).getAccountNumber();
        if (senderNumber == receiverAccountNo) throw new CustomException("Choose a different recipient account");

        Account first = accounts.findForUpdate(Math.min(senderNumber, receiverAccountNo))
                .orElseThrow(() -> new CustomException("Recipient account not found"));
        Account second = accounts.findForUpdate(Math.max(senderNumber, receiverAccountNo))
                .orElseThrow(() -> new CustomException("Recipient account not found"));
        Account sender = first.getAccountNumber() == senderNumber ? first : second;
        Account receiver = first.getAccountNumber() == receiverAccountNo ? first : second;
        if (sender.getBalance() < amount) throw new CustomException("Insufficient balance");

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);
        record(sender, amount, TransactionType.DEBIT_TRANSFER, receiverAccountNo);
        record(receiver, amount, TransactionType.CREDIT_TRANSFER, senderNumber);
        accounts.save(sender);
        accounts.save(receiver);
    }
}
