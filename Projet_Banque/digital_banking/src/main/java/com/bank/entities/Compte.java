package com.bank.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Compte {
    @Id
    private String numeroCompte;
    private double solde;
    private Date dateCreation;
    @ManyToOne
    private Client client;
    @OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
    private List<Operation> operations;
}
