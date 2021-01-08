package com.example.querydsl.entity;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberSearchCustom {
  List<MemberTeamDto> search(MemberSearchCondition condition);

  Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);


  Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
