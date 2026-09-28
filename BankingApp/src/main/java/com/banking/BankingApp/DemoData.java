package com.banking.BankingApp;

import com.banking.BankingApp.entity.Account;
import com.banking.BankingApp.entity.User;
import com.banking.BankingApp.enums.Roles;
import com.banking.BankingApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedAccounts(UserRepository users, PasswordEncoder encoder,
                                   @Value("${app.demo.enabled:false}") boolean enabled) {
        return args -> {
            if (!enabled) return;
            seed(users, encoder, "alex@aster.demo", "Alex Morgan", 9000000001L, 4100200011L, 12480.75);
            seed(users, encoder, "sam@aster.demo", "Sam Rivera", 9000000002L, 4100200012L, 3280.00);
        };
    }

    private void seed(UserRepository users, PasswordEncoder encoder, String email, String name,
                      long phone, long accountNumber, double balance) {
        if (users.existsByEmail(email)) return;
        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setPhoneNumber(phone);
        user.setAddress("Demo account");
        user.setPassword(encoder.encode("AsterDemo2026!"));
        user.setRole(Roles.USER);
        user.setEnabled(true);
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType("CHECKING");
        account.setBalance(balance);
        account.setUser(user);
        user.setAccount(account);
        users.save(user);
    }
}
