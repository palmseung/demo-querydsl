package com.example.querydsl.entity;

import java.util.List;

public interface MemberSearchCustom {
  List<MemberTeamDto> search(MemberSearchCondition condition);
}
