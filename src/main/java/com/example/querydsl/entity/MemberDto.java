package com.example.querydsl.entity;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {
  private String useranme;
  private int age;

  public MemberDto() {
  }

  @QueryProjection
  public MemberDto(String useranme, int age) {
    this.useranme = useranme;
    this.age = age;
  }
}
