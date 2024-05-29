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
@Table(name = "aat111")
@Data
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class Db2VO {
	@Id
	@Column(name = "ACYEAR")
	private String ACYEAR;
	
	@Column(name = "SLIPNO")
	private String SLIPNO;
	
	@Column(name = "ACDATE")
	private String ACDATE;
	
	@Column(name = "ACCHKDT")
	private String ACCHKDT;
	
	@Column(name = "SLIPAMT")
	private String SLIPAMT;
	
	@Column(name = "BSNFLDCOD")
	private String BSNFLDCOD;
	
	@Column(name = "DEPTCOD")
	private String DEPTCOD;
	
	@Column(name = "ACEMPNO")
	private String ACEMPNO;
	
	@Column(name = "CASHCOLID")
	private String CASHCOLID;
	
	@Column(name = "CASHTRNFID")
	private String CASHTRNFID;
	
	@Column(name = "HEADCOLID")
	private String HEADCOLID;
	
	@Column(name = "HEADBRANID")
	private String HEADBRANID;
	
	@Column(name = "ISSBSNCOD")
	private String ISSBSNCOD;
	
	@Column(name = "ISSDEPTCOD")
	private String ISSDEPTCOD;
	
	@Column(name = "PRDCOLID")
	private String PRDCOLID;
}
