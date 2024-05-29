package com.daelim.crawling.db2;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface Db2Repository extends JpaRepository<Db2VO,String>{
	@Query(value = "select * from aat111"
			+ " where acyear = '2024'"
			+ " and slipno = '103851'", nativeQuery = true)
    List<Db2VO> findAllFromDbTest();
}


