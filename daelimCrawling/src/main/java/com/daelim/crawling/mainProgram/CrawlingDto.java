package com.daelim.crawling.mainProgram;

import java.util.Objects;

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
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlingDto that = (CrawlingDto) o;
        return price == that.price &&
               Objects.equals(name, that.name) &&
               Objects.equals(link, that.link) &&
               Objects.equals(searchFrom, that.searchFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, link, searchFrom);
    }
}
