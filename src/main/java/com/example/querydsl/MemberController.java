package com.example.querydsl;

import com.example.querydsl.entity.MemberSearchCondition;
import com.example.querydsl.entity.MemberTeamDto;
import com.example.querydsl.repository.MemberJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

  private final MemberJpaRepository memberJpaRepository;

  @GetMapping("/v1/members")
  public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
    return memberJpaRepository.searchByBuilder(condition);
  }
}
