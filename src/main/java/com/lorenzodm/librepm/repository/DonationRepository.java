package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByProjectIdOrderByDonationDateDesc(String projectId);
    List<Donation> findByDonorIdOrderByDonationDateDesc(String donorId);
}
