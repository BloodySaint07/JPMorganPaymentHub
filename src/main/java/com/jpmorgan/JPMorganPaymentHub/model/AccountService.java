package com.jpmorgan.JPMorganPaymentHub.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jpmorgan.JPMorganPaymentHub.enums.AccountServiceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_service")
@Data
@NoArgsConstructor
public class AccountService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountServiceType serviceType;

    @Column(nullable = false)
    private String description;

    @ManyToMany(mappedBy = "services")
    @JsonBackReference
    private List<AccountDetails> accounts = new ArrayList<>();
}