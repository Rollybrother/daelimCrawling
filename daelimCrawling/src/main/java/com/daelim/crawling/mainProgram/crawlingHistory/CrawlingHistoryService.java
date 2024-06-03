package com.daelim.crawling.mainProgram.crawlingHistory;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.daelim.crawling.Daelim.CrawlingHistoryRepository;
import com.daelim.crawling.Daelim.CrawlingHistoryVO;
import com.daelim.crawling.Daelim.DaelimRepository;
import com.daelim.crawling.mainProgram.CrawlingDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CrawlingHistoryService {

	private final CrawlingHistoryRepository dao;
	
	public void insert(ArrayList<CrawlingDto> searchedList) {
		ArrayList<CrawlingHistoryVO> currentList = dao.findAllFromDbCrawlingHistory();
		ArrayList<CrawlingHistoryVO> finalResult = new ArrayList<>();
		for(CrawlingDto e : searchedList) {
			boolean flag = true;
			for(CrawlingHistoryVO e2 : currentList) {
				if(e.getDate().equals(e2.getDate()) && e.getLink().equals(e2.getLink())) {
					flag = false;
				}
			}
			if(flag) {
				finalResult.add(new CrawlingHistoryVO(e.getDate(),e.getName(),e.getPrice(),e.getLink()));
			}
		}
		
		dao.saveAll(finalResult);
	}
	public ArrayList<CrawlingHistoryVO> searchByDate(String date){
		return dao.findWithDate(date);
	}
	
	public ArrayList<CrawlingHistoryVO> entireList(){
		return dao.findAllFromDbCrawlingHistory();
	}
	
}
