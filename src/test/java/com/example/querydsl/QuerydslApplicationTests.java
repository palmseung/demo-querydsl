package com.example.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class QuerydslApplicationTests {

	@Autowired
	private EntityManager em;

	@Test
	void contextLoads() {
		//given
		Member member = new Member();
		em.persist(member);

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QMember qMember = new QMember("m");

		List<Member> fetch = queryFactory
				.selectFrom(qMember)
				.fetch();

		assertThat(fetch).containsExactly(member);

		for (Member m : fetch) {
			System.out.println("m = " + m);
		}

	}

}
