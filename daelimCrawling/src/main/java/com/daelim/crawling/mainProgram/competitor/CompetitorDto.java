package com.daelim.crawling.mainProgram.competitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorDto {
	
	private String productName;
	private String companyName;
	private String shoppingMall;
	private int price;
	private int averagePrice;
	public CompetitorDto(String productName, String companyName, String shoppingMall, int price) {
		super();
		this.productName = productName;
		this.companyName = companyName;
		this.shoppingMall = shoppingMall;
		this.price = price;
	}
	public CompetitorDto(String productName, String shoppingMall, int averagePrice) {
		super();
		this.productName = productName;
		this.shoppingMall = shoppingMall;
		this.averagePrice = averagePrice;
	}
	
}
