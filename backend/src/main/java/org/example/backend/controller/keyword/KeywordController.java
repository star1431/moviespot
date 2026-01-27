package org.example.backend.controller.keyword;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.keyword.KeywordResponseDto;
import org.example.backend.service.keyword.KeywordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    /** 키워드 전체 목록 조회 */
    @GetMapping
    public ResponseEntity<List<KeywordResponseDto>> getKeywords() {
        return ResponseEntity.ok(keywordService.getKeywords());
    }
}


