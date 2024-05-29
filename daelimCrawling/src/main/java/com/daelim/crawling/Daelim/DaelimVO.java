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
	
}
