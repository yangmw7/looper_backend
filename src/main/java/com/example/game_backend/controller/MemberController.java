package com.example.game_backend.controller;

import com.example.game_backend.controller.dto.JoinRequest;
import com.example.game_backend.controller.dto.LoginRequest;
import com.example.game_backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @GetMapping("/hello")
    public String getHello() {
        return "Hello from Spring Boot!";
    }

    @PostMapping("/join")
    public String join(@RequestBody JoinRequest joinRequest){

        String result = memberService.join(joinRequest);

        if("success".equalsIgnoreCase(result)){
            return "success";
        }
        else{
            return "fail";
        }
    }

    @PostMapping("/api/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        boolean result = memberService.login(loginRequest);
        return result ? "login success" : "login fail";
    }

}
