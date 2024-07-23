package com.daelim.crawling.mainProgram.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserVO, String> {
	Optional<UserVO> findById(String id);
}
