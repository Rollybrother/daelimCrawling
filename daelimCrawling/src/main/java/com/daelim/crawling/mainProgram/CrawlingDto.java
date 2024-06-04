package com.daelim.crawling.mainProgram;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlingDto {
	private String name;
	private int price;
	private String link;
	private boolean isOK = true;
	private String searchFrom;
	private String date;
	
	CrawlingDto(){
		
	}
	public CrawlingDto(String name,int price,String link,String searchFrom){
		this.name = name;
		this.price = price;
		this.link = link;
		this.searchFrom = searchFrom;
	}
}
