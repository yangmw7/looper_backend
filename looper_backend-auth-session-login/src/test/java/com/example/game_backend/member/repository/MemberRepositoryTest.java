package com.example.game_backend.member.repository;


import com.example.game_backend.repository.MemberRepository;
import com.example.game_backend.repository.entity.Member;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class MemberRepositoryTest {

    @Autowired private MemberRepository memberRepository;

    @Test
    @Transactional
    public void crudTest() {
        Member member = Member.builder()
                .email("yangminwoo@gmail.com")
                .password("12345678")
                .username("yangmw7")
                .build();

        Member saved = memberRepository.save(member);

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        System.out.println("조회된 유저: " + found.getUsername());
    }
}
