package com.banking.BankingApp.controller;

import com.banking.BankingApp.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/account")
public class AccountController {
    @Autowired
    AccountService accountService;

    @Autowired
    com.banking.BankingApp.security.CustomerUserDetails userDetails;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String authHeader) {
        var user = userDetails.getUserFromJwtToken(authHeader.substring(7));
        return ResponseEntity.ok(java.util.Map.of("fullName", user.getFullName(), "email", user.getEmail()));
    }


    @GetMapping("/details")
    public ResponseEntity<?> getDetails(@RequestHeader("Authorization") String authHeader){
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); //
        }
        return ResponseEntity.ok(accountService.getAccountDetails(jwt));
    }
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String authHeader){
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); //
        }
        return ResponseEntity.ok(accountService.getUserBalance(jwt));
    }
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@RequestHeader("Authorization") String authHeader){
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); //
        }
        return ResponseEntity.ok(accountService.getAllTransactions(jwt));
    }
}
