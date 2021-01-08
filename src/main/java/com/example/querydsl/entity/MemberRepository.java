package com.example.querydsl.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberSearchCustom {

  List<Member> findByUsername(String username);
}
