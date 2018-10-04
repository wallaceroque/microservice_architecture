package com.example.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springmvc.model.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, String> {
}
