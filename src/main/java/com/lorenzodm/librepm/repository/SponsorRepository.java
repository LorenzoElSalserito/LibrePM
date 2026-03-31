package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, String> {
    List<Sponsor> findAllByOrderByNameAsc();
}
