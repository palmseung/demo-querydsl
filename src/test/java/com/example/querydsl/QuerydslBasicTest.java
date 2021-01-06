package com.example.querydsl;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.MemberDto;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QMemberDto;
import com.example.querydsl.entity.Team;
import com.example.querydsl.entity.UserDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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

  @PersistenceUnit
  EntityManagerFactory emf;

  @Test
  void fetch_join_no_use() {
    em.flush();
    em.clear();

    Member findMember = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("페치조인 미적용 ").isFalse();
  }

  @Test
  void fetch_join_use() {
    em.flush();
    em.clear();

    Member findMember = queryFactory
        .selectFrom(member)
        .join(member.team, team).fetchJoin()
        .where(member.username.eq("member1"))
        .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("페치조인 적용").isTrue();
  }

  /*
  나이가 가장 많은 회원 조회
   */
  @Test
  void subQuery() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
            JPAExpressions
                .select(memberSub.age.max())
                .from(memberSub)
        ))
        .fetch();

    assertThat(result)
        .extracting("age")
        .containsExactly(40);
  }

  /*
  나이가 평균 이상인 회원
   */
  @Test
  void subQueryGoe() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.goe(
            JPAExpressions
                .select(memberSub.age.avg())
                .from(memberSub)
        ))
        .fetch();

    assertThat(result)
        .extracting("age")
        .containsExactly(30, 40);
  }

  @Test
  void subQueryIn() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.in(
            JPAExpressions
                .select(memberSub.age)
                .from(memberSub)
                .where(memberSub.age.gt(10))
        ))
        .fetch();

    assertThat(result)
        .extracting("age")
        .containsExactly(20, 30, 40);
  }

  @Test
  void selectSubQuery() {
    QMember memberSub = new QMember("memberSub");

    List<Tuple> result = queryFactory
        .select(member.username,
            JPAExpressions
                .select(memberSub.age.avg())
                .from(memberSub)
        )
        .from(member)
        .fetch();

    for (Tuple one : result) {
      System.out.println("tuple = " + one);
    }
  }

  /*
  JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리 (인라인뷰)는 지원하지 않음
   */


  @Test
  void basicCase() {
    List<String> result = queryFactory
        .select(member.age
            .when(10).then("열 살")
            .when(20).then("스무 살")
            .otherwise("기타")
        )
        .from(member)
        .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void complexCase() {
    List<String> result = queryFactory
        .select(
            new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21~30살")
                .otherwise("기타")
        )
        .from(member)
        .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void constant() {
    List<Tuple> result = queryFactory
        .select(member.username, Expressions.constant("A"))
        .from(member)
        .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
  }

  /*
  문자가 아닌 다른 타입들은 stringValue()로 문자로 변환 가능. 이 방법은 ENUM을 처리할 때도 자주 사용
   */
  @Test
  void concat() {
    //{username}_{age}
    List<String> fetch = queryFactory
        .select(member.username.concat("_").concat(member.age.stringValue()))
        .from(member)
        .where(member.username.eq("member1"))
        .fetch();

    for (String s : fetch) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void simpleProjection() {
    List<String> result = queryFactory
        .select(member.username)
        .from(member)
        .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  /*
   tuple이 repository 레이어에서 쓰이는 것은 상관없지만, service부터는 tuple에 의존적이지 않은 설계가좋다 => dto 로 변경해서 쓰자
   */
  @Test
  void tupleProjection() {
    List<Tuple> result = queryFactory
        .select(member.username, member.age)
        .from(member)
        .fetch();

    for (Tuple tuple : result) {
      String username = tuple.get(member.username);
      Integer age = tuple.get(member.age);
      System.out.println(username);
      System.out.println(age);
    }
  }

  @Test
  void findDtoByJPQL() {
    List<MemberDto> resultList = em.createQuery(
        "select new com.example.querydsl.entity.MemberDto(m.username, m.age) from Member m",
        MemberDto.class).getResultList();

    for (MemberDto memberDto : resultList) {
      System.out.println("memberDto = " + memberDto);
    }
  }

  @Test
  void findDtoByQuerydslUsingSetter() {
    List<MemberDto> result = queryFactory
        .select(
            Projections.bean(MemberDto.class, member.username, member.age)
        )
        .from(member)
        .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }

  /*
  field로 생성하는 건 getter, setter 없어도 됨
   */
  @Test
  void findDtoByQuerydslUsingFields() {
    List<MemberDto> result = queryFactory
        .select(
            Projections.fields(MemberDto.class, member.username, member.age)
        )
        .from(member)
        .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }


  /*
  생성자 이용은 타입 순서 잘 맞춰야 함
   */
  @Test
  void findDtoByQuerydslUsingConstructor() {
    List<MemberDto> result = queryFactory
        .select(
            Projections.constructor(MemberDto.class, member.username, member.age)
        )
        .from(member)
        .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }

  /*
  DTO 필드명과 엔티티 필드명이 다를 땐 as 사용
   */
  @Test
  void findUserDto() {
    List<UserDto> result = queryFactory
        .select(
            Projections.fields(UserDto.class,
                member.username.as("name"),
                member.age)
        )
        .from(member)
        .fetch();

    for (UserDto userDto : result) {
      System.out.println("userDto = " + userDto);
    }
  }

  @Test
  void findUserDtoWithSubQuery() {
    QMember memberSub = new QMember("memberSub");

    List<UserDto> result = queryFactory
        .select(
            Projections.fields(UserDto.class,
                member.username.as("name"),
                ExpressionUtils.as(
                    JPAExpressions.select(memberSub.age.max()).from(memberSub), "age"
                )
            )
        )
        .from(member)
        .fetch();

    for (UserDto userDto : result) {
      System.out.println("userDto = " + userDto);
    }
  }

  /*
  dto 생성자에 @QueryProjection 붙인 후 -> compileQuerydsl 클릭  : 컵파일 단계에서 dto에 관련된 에러 잡을 수 있음
  => dto가 querydsl에 의존성을 갖게 된다
   */
  @Test
  void findDtoByQueryProjection() {
    List<MemberDto> result = queryFactory
        .select(new QMemberDto(member.username, member.age))
        .from(member)
        .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }


}
