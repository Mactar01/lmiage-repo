package com.bank.services;

import com.bank.entities.Client;
import com.bank.entities.Compte;
import com.bank.entities.Operation;
import com.bank.entities.Retrait;
import com.bank.entities.Depot;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class BanqueServiceImpl implements BanqueService {
    private final EntityManagerFactory emf;

    public BanqueServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Client saveClient(Client client) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(client);
            em.getTransaction().commit();
            return client;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> listClients() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery("SELECT c FROM Client c", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Compte saveCompte(Compte compte, Long clientId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Client client = em.find(Client.class, clientId);
            if (client == null) {
                throw new RuntimeException("Client non trouvé");
            }

            compte.setNumeroCompte(generateAccountNumber());
            compte.setClient(client);
            compte.setDateCreation(new Date());
            
            em.persist(compte);
            em.getTransaction().commit();
            return compte;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Compte> listComptes() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery("SELECT c FROM Compte c", Compte.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void depot(String numeroCompte, double montant) {
        if (montant <= 0) {
            throw new RuntimeException("Le montant doit être positif");
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Compte compte = em.createQuery("SELECT c FROM Compte c WHERE c.numeroCompte = :numero", Compte.class)
                    .setParameter("numero", numeroCompte)
                    .getSingleResult();

            Depot depot = new Depot();
            depot.setDateOperation(new Date());
            depot.setMontant(montant);
            depot.setCompte(compte);

            compte.setSolde(compte.getSolde() + montant);

            em.persist(depot);
            em.merge(compte);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void retrait(String numeroCompte, double montant) {
        if (montant <= 0) {
            throw new RuntimeException("Le montant doit être positif");
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Compte compte = em.createQuery("SELECT c FROM Compte c WHERE c.numeroCompte = :numero", Compte.class)
                    .setParameter("numero", numeroCompte)
                    .getSingleResult();

            if (compte.getSolde() < montant) {
                throw new RuntimeException("Solde insuffisant");
            }

            Retrait retrait = new Retrait();
            retrait.setDateOperation(new Date());
            retrait.setMontant(montant);
            retrait.setCompte(compte);

            compte.setSolde(compte.getSolde() - montant);

            em.persist(retrait);
            em.merge(compte);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void virement(String numeroCompteSource, String numeroCompteDest, double montant) {
        if (montant <= 0) {
            throw new RuntimeException("Le montant doit être positif");
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Compte compteSource = em.createQuery("SELECT c FROM Compte c WHERE c.numeroCompte = :numero", Compte.class)
                    .setParameter("numero", numeroCompteSource)
                    .getSingleResult();

            Compte compteDest = em.createQuery("SELECT c FROM Compte c WHERE c.numeroCompte = :numero", Compte.class)
                    .setParameter("numero", numeroCompteDest)
                    .getSingleResult();

            if (compteSource.getSolde() < montant) {
                throw new RuntimeException("Solde insuffisant");
            }

            // Effectuer le retrait
            Retrait retrait = new Retrait();
            retrait.setDateOperation(new Date());
            retrait.setMontant(montant);
            retrait.setCompte(compteSource);
            compteSource.setSolde(compteSource.getSolde() - montant);

            // Effectuer le dépôt
            Depot depot = new Depot();
            depot.setDateOperation(new Date());
            depot.setMontant(montant);
            depot.setCompte(compteDest);
            compteDest.setSolde(compteDest.getSolde() + montant);

            em.persist(retrait);
            em.persist(depot);
            em.merge(compteSource);
            em.merge(compteDest);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Operation> listOperations(String numeroCompte, int page, int size) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                "SELECT o FROM Operation o WHERE o.compte.numeroCompte = :numero ORDER BY o.dateOperation DESC",
                Operation.class
            )
            .setParameter("numero", numeroCompte)
            .setFirstResult(page * size)
            .setMaxResults(size);
            
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    private String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
