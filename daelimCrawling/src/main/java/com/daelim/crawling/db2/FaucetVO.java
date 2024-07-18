package com.daelim.crawling.db2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pkt111")
@Data
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class FaucetVO {
	static final String category = "수전";
	
	@Id
	@Column(name = "PARTNAMEK")
	private String name;
}
