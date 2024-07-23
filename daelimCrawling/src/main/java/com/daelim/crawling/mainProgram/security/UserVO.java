package com.daelim.crawling.mainProgram.security;

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
@Table(name = "db_crawling_user")
@Data
@NoArgsConstructor @AllArgsConstructor
public class UserVO {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private int role;

    @Column(name = "searchType", nullable = true)
    private String searchType;

    @Column(name = "selectedItems", nullable = true)
    private String selectedItems; // JSON 형태로 저장
}
