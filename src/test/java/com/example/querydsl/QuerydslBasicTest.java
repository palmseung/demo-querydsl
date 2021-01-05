package com.example.querydsl;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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

  @Test
  void resultFetch() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();

    Member fetchOne = queryFactory
        .selectFrom(member)
        .fetchOne();

    Member fetchFirst = queryFactory
        .selectFrom(member)
        .fetchFirst();

    QueryResults<Member> fetchResults = queryFactory
        .selectFrom(member)
        .fetchResults();   //성능이 중요한 query에서는 total과 count 쿼리 따로 쓰는 게 좋음

    fetchResults.getTotal();
    List<Member> results = fetchResults.getResults();

    long count = queryFactory
        .selectFrom(member)
        .fetchCount();
  }

  /*
  회원 정렬 순서
  1. 나이 내림차순
  2. 회원이름 올림차순
  // 단 2에서 회원이름이 없으면 마지막에 (nulls last)
   */
  @Test
  void sort() {
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(100))
        .orderBy(
            member.age.desc(),
            member.username.asc().nullsLast()
        )
        .fetch();

    Member member5 = result.get(0);
    Member member6 = result.get(1);
    Member memberNull = result.get(2);

    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
  }

  @Test
  void paging() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetch();

    assertThat(fetch.size()).isEqualTo(2);
  }


  @Test
  void paging2() {
    QueryResults<Member> fetchResults = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetchResults();

    assertThat(fetchResults.getTotal()).isEqualTo(4);
    assertThat(fetchResults.getLimit()).isEqualTo(2);
    assertThat(fetchResults.getOffset()).isEqualTo(1);
    assertThat(fetchResults.getResults()).hasSize(2);
  }

  @Test
  void aggregation() {
    List<Tuple> fetch = queryFactory
        .select(
            member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
        .from(member)
        .fetch();

    Tuple tuple = fetch.get(0);
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
  }


  /*
  각 팀의 이름과 평균 연령을 구해라
   */
  @Test
  void groupBy() {
    List<Tuple> result = queryFactory
        .select(team.name, member.age.avg())
        .from(member)
        .join(member.team, team)
        .groupBy(team.name)
        .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);

    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);
  }

  @Test
  void join() {
    List<Member> result = queryFactory
        .selectFrom(member)
        .join(member.team, team)
        .where(team.name.eq("teamA"))
        .fetch();

    assertThat(result)
        .extracting("username")
        .containsExactly("member1", "member2");
  }

  /*
  세타 조인
   */
  @Test
  void thetaJoin() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));

    List<Member> fetch = queryFactory
        .select(member)
        .from(member, team)
        .where(member.username.eq(team.name))
        .fetch();

    assertThat(fetch)
        .extracting("username")
        .containsExactly("teamA", "teamB");
  }

  /*
  회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
  JPQL : select m, t from Member m left join m.team on t.name= 'teamA'
   */
  @Test
  void joinOnFiltering() {
    List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .join(member.team, team)
//        .on(team.name.eq("teamA"))
        .where(team.name.eq("teamA"))
        .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
  }

  /*
  연관관계가 없는 엔티티 외부 조인
  회원의 이름과 팀 이름이 같은 회원 조인
   */
  @Test
  void join_on_no_relation() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Tuple> fetch = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(team).on(member.username.eq(team.name))
        .fetch();

    for (Tuple tuple : fetch) {
      System.out.println("tuple = " + tuple);
    }

  }
}
