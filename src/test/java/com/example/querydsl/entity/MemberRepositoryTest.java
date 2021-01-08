package com.example.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

  @Autowired
  EntityManager em;

  @Autowired
  private MemberRepository memberRepository;

  @Test
  void basicTest() {
    Member member = new Member("member1", 10);
    Member save = memberRepository.save(member);

    Member member1 = memberRepository.findById(member.getId()).get();
    assertThat(member1).isEqualTo(member);

    List<Member> result1 = memberRepository.findAll();
    assertThat(result1).containsExactly(member);

    List<Member> result2 = memberRepository.findByUsername("member1");
    assertThat(result2).containsExactly(member);
  }
}