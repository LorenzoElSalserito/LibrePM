package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, String> {
    List<CurrencyRate> findByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(String from, String to);
}
