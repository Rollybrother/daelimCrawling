package com.daelim.crawling.mainProgram;

import com.daelim.crawling.mainProgram.sellerDetail.SellerDetailDto;

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
	public SellerDetailDto sellerDetailDto;
	
	CrawlingDto(){
		
	}
	public CrawlingDto(String name,int price,String link,String searchFrom){
		this.name = name;
		this.price = price;
		this.link = link;
		this.searchFrom = searchFrom;
	}
	
	public CrawlingDto(String name,int price,String link,String searchFrom,SellerDetailDto sellerDetailDto){
		this.name = name;
		this.price = price;
		this.link = link;
		this.searchFrom = searchFrom;
		this.sellerDetailDto = sellerDetailDto;
	}
}
