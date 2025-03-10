package com.bank.repositories;

import com.bank.entities.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    Page<Operation> findByCompteNumeroCompte(String numeroCompte, Pageable pageable);
}
