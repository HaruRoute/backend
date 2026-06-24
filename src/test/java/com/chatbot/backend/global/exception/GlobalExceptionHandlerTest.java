package com.chatbot.backend.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ==================== MethodArgumentNotValidException (400) ====================

    @Test
    @DisplayName("@Valid 실패 - 필드 에러 메시지 반환 → 400")
    void handleValidation_필드에러_400() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "id", "아이디를 입력해주세요."));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("아이디를 입력해주세요.");
    }

    @Test
    @DisplayName("@Valid 실패 - 여러 에러 중 첫 번째 메시지만 반환")
    void handleValidation_여러에러_첫번째반환() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "id", "첫 번째 오류"));
        bindingResult.addError(new FieldError("target", "pw", "두 번째 오류"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("첫 번째 오류");
    }

    @Test
    @DisplayName("@Valid 실패 - 에러 메시지 없으면 기본 메시지 반환")
    void handleValidation_기본메시지_반환() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("입력값이 유효하지 않습니다.");
    }

    // ==================== HttpMessageNotReadableException (400) ====================

    @Test
    @DisplayName("잘못된 JSON 바디 → 400")
    void handleMessageNotReadable_400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<Map<String, String>> response = handler.handleMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("요청 본문을 읽을 수 없습니다.");
    }

    // ==================== MethodArgumentTypeMismatchException (400) ====================

    @Test
    @DisplayName("경로 변수 타입 불일치 (예: /api/posts/abc) → 400")
    void handleTypeMismatch_400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).contains("id");
    }

    // ==================== MissingServletRequestParameterException (400) ====================

    @Test
    @DisplayName("필수 쿼리 파라미터 누락 → 400")
    void handleMissingParam_400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("userId", "String");

        ResponseEntity<Map<String, String>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).contains("userId");
    }

    // ==================== HttpRequestMethodNotSupportedException (405) ====================

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드 → 405")
    void handleMethodNotSupported_405() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PATCH");

        ResponseEntity<Map<String, String>> response = handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().get("message")).contains("PATCH");
    }

    // ==================== IllegalArgumentException (400) ====================

    @Test
    @DisplayName("IllegalArgumentException → 400")
    void handleIllegalArgument_400() {
        IllegalArgumentException ex = new IllegalArgumentException("이미 존재하는 아이디입니다.");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("이미 존재하는 아이디입니다.");
    }

    // ==================== IllegalStateException (403) ====================

    @Test
    @DisplayName("IllegalStateException (권한 없음) → 403")
    void handleIllegalState_403() {
        IllegalStateException ex = new IllegalStateException("권한이 없습니다.");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message")).isEqualTo("권한이 없습니다.");
    }

    @Test
    @DisplayName("IllegalStateException (관리자 전용) → 403")
    void handleIllegalState_관리자전용_403() {
        IllegalStateException ex = new IllegalStateException("관리자만 접근 가능합니다.");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message")).isEqualTo("관리자만 접근 가능합니다.");
    }

    // ==================== NoSuchElementException (404) ====================

    @Test
    @DisplayName("NoSuchElementException → 404")
    void handleNoSuchElement_404() {
        NoSuchElementException ex = new NoSuchElementException("게시글을 찾을 수 없습니다.");

        ResponseEntity<Map<String, String>> response = handler.handleNoSuchElement(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("게시글을 찾을 수 없습니다.");
    }

    // ==================== Exception (500) ====================

    @Test
    @DisplayName("예상치 못한 예외 → 500, 고정 메시지")
    void handleGeneral_500() {
        Exception ex = new RuntimeException("DB 연결 실패");

        ResponseEntity<Map<String, String>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // 내부 오류 메시지는 클라이언트에 노출하지 않음
        assertThat(response.getBody().get("message")).isEqualTo("서버 오류가 발생했습니다.");
        assertThat(response.getBody().get("message")).doesNotContain("DB 연결 실패");
    }

    // ==================== 응답 형식 일관성 ====================

    @Test
    @DisplayName("모든 오류 응답은 'message' 키를 가져야 함")
    void 응답형식_일관성() {
        assertThat(handler.handleIllegalArgument(new IllegalArgumentException("x")).getBody()).containsKey("message");
        assertThat(handler.handleIllegalState(new IllegalStateException("x")).getBody()).containsKey("message");
        assertThat(handler.handleNoSuchElement(new NoSuchElementException("x")).getBody()).containsKey("message");
        assertThat(handler.handleGeneral(new RuntimeException("x")).getBody()).containsKey("message");
        assertThat(handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("PUT")).getBody()).containsKey("message");
    }
}
