package com.example.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.MemberSearchCondition;
import com.example.querydsl.entity.MemberTeamDto;
import com.example.querydsl.entity.Team;
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

  @Test
  void searchByBuilder() {

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);

    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

    assertThat(result)
        .extracting("username")
        .containsExactly("member4");
  }

  @Test
  void searchByBuilder2() {

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);

    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setTeamName("teamB");

    List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

    assertThat(result)
        .extracting("username")
        .containsExactly("member3", "member4");
  }
}