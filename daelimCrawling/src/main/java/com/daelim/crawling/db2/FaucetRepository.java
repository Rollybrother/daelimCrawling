package com.daelim.crawling.db2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface FaucetRepository extends JpaRepository<FaucetVO,String>{
	@Query(value = "select partnamek from PKT111", nativeQuery = true)
    ArrayList<FaucetVO> findNameFromDbFaucet();
}
