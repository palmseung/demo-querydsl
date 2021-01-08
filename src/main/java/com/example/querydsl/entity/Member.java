package com.example.querydsl.entity;

import com.example.querydsl.entity.Team;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;
  private String username;
  private Integer age;

  @Override
  public String toString() {
    return "Member{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", age=" + age +
        '}';
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private Team team;

  public Member(String username) {
    this(username, 0);
  }

  public Member(String username, Integer age) {
    this.username = username;
    this.age = age;
  }

  public Member(String username, Integer age, Team team) {
    this.username = username;
    this.age = age;
    this.team = team;
  }
}
