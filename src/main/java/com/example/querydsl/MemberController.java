package com.example.querydsl;

import com.example.querydsl.entity.MemberRepository;
import com.example.querydsl.entity.MemberSearchCondition;
import com.example.querydsl.entity.MemberTeamDto;
import com.example.querydsl.repository.MemberJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

  private final MemberJpaRepository memberJpaRepository;
  private final MemberRepository memberRepository;

  @GetMapping("/v1/members")
  public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
    return memberJpaRepository.searchByBuilder(condition);
  }

  @GetMapping("/v2/members")
  public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchPageSimple(condition, pageable);
  }

  @GetMapping("/v3/members")
  public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchPageComplex(condition, pageable);
  }
}
