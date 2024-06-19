package com.daelim.crawling.Daelim;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.beust.jcommander.Parameter;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface DaelimRepository extends JpaRepository<DaelimVO,Integer>{
	@Query(value = "SELECT * FROM db_crawling", nativeQuery = true)
	ArrayList<DaelimVO> findAllFromDbCrawling();
	
	@Modifying
    @Query(value = "INSERT INTO db_crawling (name, price,searchLimit,competitor1Product,competitor1Name,competitor2Product,competitor2Name) "
    		+ "VALUES (:name, :price,:searchLimit,:competitor1Product,:competitor1Name,:competitor2Product,:competitor2Name)", nativeQuery = true)
    Integer insertForDbCrawling(@Param("name")String name, @Param("price")int price,@Param("searchLimit")int searchLimit,
    		@Param("competitor1Product")String competitor1Product,@Param("competitor1Name")String competitor1Name,
    		@Param("competitor2Product")String competitor2Product,@Param("competitor2Name")String competitor2Name);
	
	@Query(value = "SELECT `index` FROM db_crawling WHERE name = :name LIMIT 1", nativeQuery = true)
	Integer findIndexByName(@Param("name") String name);
	
	@Modifying
    @Query(value = "DELETE FROM db_crawling WHERE `index` IN (:indices)", nativeQuery = true)
    void deleteByIndices(@Param("indices") List<Integer> indices);
	
	// 가장 큰 index 값을 가져오는 메서드 추가
    @Query(value = "SELECT MAX(`index`) FROM db_crawling", nativeQuery = true)
    Integer findMaxIndex();
}

