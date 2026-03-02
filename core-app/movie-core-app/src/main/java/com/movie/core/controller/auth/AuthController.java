package com.movie.core.controller.auth;

import com.movie.core.controller.auth.request.LoginRequest;
import com.movie.core.controller.auth.request.RegisterRequest;
import com.movie.core.security.session.SessionAuthService;
import com.movie.core.usecase.auth.CheckEmailAvailabilityUseCase;
import com.movie.core.usecase.auth.LoginUseCase;
import com.movie.core.usecase.auth.RegisterUseCase;
import com.movie.core.usecase.auth.SocialLoginUseCase;
import com.movie.core.usecase.auth.port.SocialAuthorizationPort;
import com.movie.core.usecase.auth.port.SocialAuthorizationPort.SocialAuthorizationInfo;
import com.movie.user.account.Account;
import com.movie.user.account.social.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/회원가입 API")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final SocialLoginUseCase socialLoginUseCase;
    private final CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;
    private final SessionAuthService sessionAuthService;
    private final SocialAuthorizationPort socialAuthorizationPort;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 서버 세션을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Login completed\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 오류",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"UNAUTHORIZED\",\"message\":\"Invalid credentials\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잠긴 계정 또는 비활성 계정",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"FORBIDDEN\",\"message\":\"Account is not active\"}")
                    )
            )
    })
    public ResponseEntity<SimpleMessageResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Account account = loginUseCase.login(
                request.email(),
                request.password()
        );
        sessionAuthService.establishSession(httpServletRequest, account);
        return ResponseEntity.ok(new SimpleMessageResponse("SUCCESS", "Login completed"));
    }


    @PostMapping("/register")
    @Operation(summary = "일반 회원가입", description = "이메일/비밀번호 기반 계정을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Registration completed\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 이메일",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"CONFLICT\",\"message\":\"Email already exists\"}")
                    )
            )
    })
    public ResponseEntity<SimpleMessageResponse> register(@RequestBody RegisterRequest request) {
        registerUseCase.register(
                request.email(),
                request.password(),
                request.role()
        );
        return ResponseEntity.ok(new SimpleMessageResponse("SUCCESS", "Registration completed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "일반/소셜 로그인 공통으로 서버 세션을 무효화하고 쿠키를 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Logout completed\"}")
                    )
            )
    })
    public ResponseEntity<SimpleMessageResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        clearSessionCookie(request, response);
        return ResponseEntity.ok(new SimpleMessageResponse("SUCCESS", "Logout completed"));
    }

    @GetMapping("/logout/social")
    @Operation(summary = "소셜 로그아웃", description = "서버 세션을 무효화하고 provider별 로그아웃 페이지로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "소셜 로그아웃 리다이렉트 (Location 헤더)",
                    content = @Content(examples = @ExampleObject(value = "Body 없음 (redirect 응답)"))
            )
    })
    public ResponseEntity<Void> socialLogout(
            @Parameter(description = "소셜 제공자", example = "kakao")
            @RequestParam String provider,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SocialProvider resolvedProvider = resolveProvider(provider);
        clearSessionCookie(request, response);
        String logoutUrl = socialAuthorizationPort.resolveLogoutUrl(resolvedProvider);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(logoutUrl))
                .build();
    }

    @GetMapping("/login/social")
    @Operation(summary = "소셜 로그인 시작", description = "서버가 provider별 소셜 인증 페이지로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "소셜 인증 페이지로 리다이렉트 (Location 헤더)",
                    content = @Content(examples = @ExampleObject(value = "Body 없음 (redirect 응답)"))
            )
    })
    public ResponseEntity<Void> socialStart(
            @Parameter(description = "소셜 제공자", example = "kakao")
            @RequestParam String provider,
            HttpServletRequest request
    ) {
        SocialProvider resolvedProvider = resolveProvider(provider);
        SocialAuthorizationInfo info = socialAuthorizationPort.buildAuthorizationInfo(resolvedProvider, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(info.authorizationUrl()))
                .build();
    }

    @GetMapping("/login/social/callback/{provider}")
    @Operation(summary = "소셜 콜백 처리", description = "소셜 인가코드를 받아 서버에서 회원가입/로그인 후 프론트로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "성공/실패 페이지로 리다이렉트 (Location 헤더)",
                    content = @Content(examples = @ExampleObject(value = "Body 없음 (redirect 응답)"))
            )
    })
    public void socialCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
            SocialProvider resolvedProvider = resolveProvider(provider);
        try {
            socialAuthorizationPort.validateState(resolvedProvider, state, request);
            Account account = socialLoginUseCase.loginOrRegister(resolvedProvider, code, state);
            sessionAuthService.establishSession(request, account);
            response.sendRedirect(socialAuthorizationPort.successUri());
        } catch (RuntimeException ex) {
            log.warn("Social login failed: provider={}, reason={}", resolvedProvider, ex.getMessage());
            response.sendRedirect(socialAuthorizationPort.failureUri());
        }
    }

    @GetMapping("/register/email-available")
    @Operation(summary = "이메일 중복 확인", description = "회원가입 가능 이메일인지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = EmailAvailabilityResponse.class),
                            examples = @ExampleObject(value = "{\"email\":\"test@example.com\",\"available\":true}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이메일 형식 오류",
                    content = @Content(
                            schema = @Schema(implementation = SimpleMessageResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"BAD_REQUEST\",\"message\":\"Invalid email format\"}")
                    )
            )
    })
    public ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
            @Parameter(description = "확인할 이메일", example = "test@example.com")
            @RequestParam String email
    ) {
        boolean available = checkEmailAvailabilityUseCase.isAvailable(email);
        return ResponseEntity.ok(new EmailAvailabilityResponse(email, available));
    }

    @Schema(description = "공통 메시지 응답")
    public record SimpleMessageResponse(
            @Schema(description = "요청 처리 상태", example = "SUCCESS")
            String status,
            @Schema(description = "응답 메시지", example = "Operation completed")
            String message
    ) {
    }

    @Schema(description = "이메일 중복 확인 응답")
    public record EmailAvailabilityResponse(
            @Schema(description = "조회한 이메일", example = "test@example.com")
            String email,
            @Schema(description = "사용 가능 여부", example = "true")
            boolean available
    ) {
    }

    private SocialProvider resolveProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider is required");
        }
        return SocialProvider.valueOf(provider.trim().toUpperCase());
    }

    private void clearSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}
