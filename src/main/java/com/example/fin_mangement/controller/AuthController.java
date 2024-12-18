package com.example.fin_mangement.controller;

import com.example.fin_mangement.dto.AuthRequest;
import com.example.fin_mangement.dto.AuthResponse;
import com.example.fin_mangement.dto.UserDto;
import com.example.fin_mangement.entity.User;
import com.example.fin_mangement.repository.UserRepository;
import com.example.fin_mangement.service.CustomUserDetailsService;
import com.example.fin_mangement.service.UserService;
import com.example.fin_mangement.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    // 로그인 요청 처리 및 JWT 토큰 발급
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Validated UserDto userDTO) {
        // 1. 중복 체크
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 존재하는 사용자 이름입니다."));
        }

        // 2. 비밀번호 암호화 및 User 엔티티 생성
        User newUser = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .enabled(true)
                .build();

        // 3. 저장
        userRepository.save(newUser);

        // 4. 성공 메시지 반환
        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt));
        }catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new AuthResponse("잘못된 증명입니다."));
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<UserDto> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userDto);
    }
}