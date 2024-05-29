package com.daelim.crawling.Daelim;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface DaelimRepository extends JpaRepository<DaelimVO,Integer>{
	@Query(value = "SELECT * FROM db_crawling", nativeQuery = true)
	ArrayList<DaelimVO> findAllFromDbCrawling();
	
	@Modifying
    @Query(value = "INSERT INTO db_crawling (name, price) VALUES (:name, :price)", nativeQuery = true)
    Integer insertForDbCrawling(@Param("name")String name, @Param("price")int price);
	
	@Query(value = "SELECT `index` FROM db_crawling WHERE name = :name LIMIT 1", nativeQuery = true)
	Integer findIndexByName(@Param("name") String name);
	
	@Modifying
    @Query(value = "DELETE FROM db_crawling WHERE `index` IN (:indices)", nativeQuery = true)
    void deleteByIndices(@Param("indices") List<Integer> indices);
}

