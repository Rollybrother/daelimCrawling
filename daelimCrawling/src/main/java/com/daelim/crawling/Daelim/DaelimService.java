package com.daelim.crawling.Daelim;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DaelimService {
	
	private final DaelimRepository daelimRepository;
	
	public ArrayList<DaelimVO> findAllFromDbTest() {
        return this.daelimRepository.findAllFromDbCrawling();
    }
}
