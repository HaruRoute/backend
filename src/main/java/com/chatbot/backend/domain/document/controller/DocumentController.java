package com.chatbot.backend.domain.document.controller;

import com.chatbot.backend.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final RestTemplate restTemplate;
    private final UserService userService;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private void requireAdmin(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (!userService.isAdmin(userId)) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    @GetMapping
    public ResponseEntity<Map<String, Object>> listDocuments(HttpServletRequest request) {
        requireAdmin(request);
        Map<String, Object> result = restTemplate.getForObject(aiServerUrl + "/documents", Map.class);
        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("unchecked")
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String filename, HttpServletRequest request) {
        requireAdmin(request);
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(
                aiServerUrl + "/documents/" + filename,
                HttpMethod.DELETE,
                null,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );
        return ResponseEntity.ok(result.getBody());
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        requireAdmin(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Requested-With", "XMLHttpRequest");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
        };
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> result = restTemplate.postForEntity(aiServerUrl + "/upload", requestEntity, Map.class);
        return ResponseEntity.ok(result.getBody());
    }
}
