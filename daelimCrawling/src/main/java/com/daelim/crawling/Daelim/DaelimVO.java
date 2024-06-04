package com.daelim.crawling.Daelim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "db_crawling")
@Data
@NoArgsConstructor @AllArgsConstructor
public class DaelimVO {
	@Id
	@Column(name = "index")
	private int index;
	
    @Column(name = "name")
	private String name;
	
	@Column(name = "price")
	private int price;
	
	@Column(name = "searchLimit")
	private int searchLimit;

	@Column(name = "competitor1Product")
	private String competitor1Product="";
	
	@Column(name = "competitor1Name")
	private String competitor1Name="";
	
	@Column(name = "competitor2Product")
	private String competitor2Product="";
	
	@Column(name = "competitor2Name")
	private String competitor2Name="";
	
	public DaelimVO(int index, String name, int price, int searchLimit) {
		super();
		this.index = index;
		this.name = name;
		this.price = price;
		this.searchLimit = searchLimit;
	}
}
