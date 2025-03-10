package com.bank.entities;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DEPOT")
@NoArgsConstructor
public class Depot extends Operation {
}
