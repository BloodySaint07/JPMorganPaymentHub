package com.jpmorgan.JPMorganPaymentHub.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_details")
@Data
@NoArgsConstructor
public class AccountDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private double balance;

    @Column(length = 16)
    private String creditCardNumber;

    @Column(length = 16)
    private String debitCardNumber;

    @Column(nullable = false)
    private String accountType;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<TransactionDetail> transactions = new ArrayList<>();
    @ManyToMany
    @JoinTable(name="account_service_mapping",
            joinColumns =@JoinColumn(name="account_id"),
            inverseJoinColumns  =@JoinColumn(name="service_id"))
    @JsonManagedReference
    private List<AccountService> services=new ArrayList<>();
}