package com.daelim.crawling.Daelim;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface CrawlingHistoryRepository extends JpaRepository<CrawlingHistoryVO,Integer>{
	@Query(value = "SELECT * FROM db_crawling_history", nativeQuery = true)
	ArrayList<CrawlingHistoryVO> findAllFromDbCrawlingHistory();
	
	@Query(value = "SELECT * FROM db_crawling_history WHERE date = :date", nativeQuery = true)
    ArrayList<CrawlingHistoryVO> findWithDate(@Param("date") String date);
	
	//insert 메서드는 자동으로 제공됨
}
