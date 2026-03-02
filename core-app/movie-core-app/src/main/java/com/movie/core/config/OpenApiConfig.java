package com.movie.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Movie Auth API",
                version = "v1",
                description = "회원가입/로그인/OAuth2 인증 관련 API 문서",
                contact = @Contact(name = "Movie Team"),
                license = @License(name = "Private")
        )
)
public class OpenApiConfig {
}
