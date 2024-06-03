package com.daelim.crawling.Daelim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "db_crawling_history")
@Data
public class CrawlingHistoryVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "index")
	private int index;
	
    @Column(name = "date")
    private String date;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "price")
    private int price;
    
    @Column(name = "link", length = 2048) 
    private String link;

    // 기본 생성자 추가
    public CrawlingHistoryVO() {
    }

    // 매개변수가 있는 생성자 유지
    public CrawlingHistoryVO(String date, String name, int price, String link) {
        this.date = date;
        this.name = name;
        this.price = price;
        this.link = link;
    }
}
