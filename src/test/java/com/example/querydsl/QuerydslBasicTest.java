package com.example.querydsl;

import static com.example.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

  private JPAQueryFactory queryFactory;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void before() {
    queryFactory = new JPAQueryFactory(em);

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
  }

  @Test
  void startJPQL() {
    //find member1 using JPQL
    Member findMember = em
        .createQuery("select m from Member m where m.username = :username", Member.class)
        .setParameter("username", "member1")
        .getSingleResult();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void startQuerydsl() {
    //find member1 using querydsl
    Member findMember = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void search() {
    Member search = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1").and(member.age.eq(10)))
        .fetchOne();

    assertThat(search.getUsername()).isEqualTo("member1");
  }

  @Test
  void search2() {
    Member search = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1")
            .and(member.age.between(10, 30)))
        .fetchOne();

    assertThat(search.getUsername()).isEqualTo("member1");
  }

  @Test
  void search3() {
    Member search = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("member1"),
            member.age.between(10, 30)
        )
        .fetchOne();

    assertThat(search.getUsername()).isEqualTo("member1");
  }

  @Test
  void search4() {
    Member search = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("member1"),
            member.age.between(10, 30),
            null // 중간에 null이 들어가면 무시하고 실행 (동적쿼리 할 때 이 기능이 빛을 발함)
        )
        .fetchOne();

    assertThat(search.getUsername()).isEqualTo("member1");
  }
}
