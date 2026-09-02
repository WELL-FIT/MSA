package com.lecture.user.controller;

import com.lecture.user.dto.UserDto;
import com.lecture.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /users/register - 회원가입 (인증 불필요)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> register(
            @Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users/{id} - 사용자 조회 (인증 필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getUser(
            @PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users/me - 내 정보 조회
     * API Gateway가 전달한 X-User-Id 헤더(숫자 userId)를 사용
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getMe(
            @RequestHeader("X-User-Id") Long userId) {

        UserDto.UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users/internal/{id} - 서비스 간 내부 호출용 (Client Credentials)
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserInternal(@PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}