package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, String> {
    List<Donor> findAllByOrderByNameAsc();
}
