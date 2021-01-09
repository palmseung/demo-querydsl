package com.example.querydsl.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberSearchCustom, QuerydslPredicateExecutor<Member> {

  List<Member> findByUsername(String username);
}
