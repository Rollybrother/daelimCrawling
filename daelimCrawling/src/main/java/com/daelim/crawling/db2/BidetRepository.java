package com.daelim.crawling.db2;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface BidetRepository extends JpaRepository<BidetVO,String>{
	@Query(value = "select partnamek from PDT211", nativeQuery = true)
    ArrayList<BidetVO> findNameFromDbBidet();
}
