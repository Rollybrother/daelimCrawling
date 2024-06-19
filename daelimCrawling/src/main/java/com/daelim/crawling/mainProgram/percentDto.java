package com.daelim.crawling.mainProgram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class percentDto {
	private String shoppingMall;
	private int percent;
	private float numerator;
	private float denominator;
	public percentDto() {
    }
	public percentDto(String shoppingMall, int percent, float numerator, float denominator) {
		super();
		this.shoppingMall = shoppingMall;
		this.percent = percent;
		this.numerator = numerator;
		this.denominator = denominator;
	}
}
