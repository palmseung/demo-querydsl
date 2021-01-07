package com.example.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.querydsl.entity.Member;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

  @Autowired
  EntityManager em;

  @Autowired MemberJpaRepository memberJpaRepository;

  @Test
  void basicTest() {
    Member member = new Member("member1", 10);
    memberJpaRepository.save(member);

    Member member1 = memberJpaRepository.findById(member.getId()).get();
    assertThat(member1).isEqualTo(member);

    List<Member> result1 = memberJpaRepository.findAll();
    assertThat(result1).containsExactly(member);

    List<Member> result2 = memberJpaRepository.findByUsername("member1");
    assertThat(result2).containsExactly(member);

    List<Member> result11 = memberJpaRepository.findAll_Querydsl();
    assertThat(result11).containsExactly(member);

    List<Member> result22 = memberJpaRepository.findByUsername_Querydsl("member1");
    assertThat(result22).containsExactly(member);
  }

}