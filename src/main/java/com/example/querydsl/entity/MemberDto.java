package com.example.querydsl.entity;

import lombok.Data;

@Data
public class MemberDto {
  private String useranme;
  private int age;

  public MemberDto() {
  }

  public MemberDto(String useranme, int age) {
    this.useranme = useranme;
    this.age = age;
  }
}
