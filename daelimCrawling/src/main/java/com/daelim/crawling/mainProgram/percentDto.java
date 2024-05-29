package com.daelim.crawling.mainProgram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class percentDto {
	private String shoppingMall;
	private int percent;
	public percentDto() {
    }
	percentDto(String shoppingMall,int percent){
		this.shoppingMall = shoppingMall;
		this.percent = percent;
	}
}
