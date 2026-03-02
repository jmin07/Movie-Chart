package com.movie.core.controller.home;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/home")
    @Operation(summary = "홈 헬스 체크", description = "서버 기본 응답을 확인하는 간단한 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 응답",
                    content = @Content(
                            schema = @Schema(implementation = HomeResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"success\"}")
                    )
            )
    })
    public ResponseEntity<HomeResponse> home() {
        return ResponseEntity.ok(new HomeResponse("SUCCESS", "success"));
    }

    public record HomeResponse(
            String status,
            String message
    ) {
    }
}
