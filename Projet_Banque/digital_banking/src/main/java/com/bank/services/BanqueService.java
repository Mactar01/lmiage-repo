package com.bank.services;

import com.bank.entities.Client;
import com.bank.entities.Compte;
import com.bank.entities.Operation;
import java.util.List;

public interface BanqueService {
    Client saveClient(Client client);
    List<Client> listClients();
    
    Compte saveCompte(Compte compte, Long clientId);
    List<Compte> listComptes();
    
    void depot(String numeroCompte, double montant);
    void retrait(String numeroCompte, double montant);
    void virement(String numeroCompteSource, String numeroCompteDest, double montant);
    
    List<Operation> listOperations(String numeroCompte, int page, int size);
}
