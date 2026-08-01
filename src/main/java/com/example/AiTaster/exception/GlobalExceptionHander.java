package com.example.AiTaster.exception;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
@ControllerAdvice  // Áp dụng cho tất cả controller trong hệ thống.
@Slf4j
public class GlobalExceptionHander {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse<Object>> handleUnreadableRequestBody(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest()
                .body(APIResponse.response(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dữ liệu yêu cầu không hợp lệ",
                        null
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<Object>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.response(
                        HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền thực hiện thao tác này",
                        null
                ));
    }

    // Bắt lỗi nghiệp vụ do mình tự throw.
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<APIResponse <Object>> handlerGlobal(GlobalException exception) {
        int status = (exception.getCode() != null)
                ? exception.getCode()
                : HttpStatus.INTERNAL_SERVER_ERROR.value(); // Nếu có code thì dùng code đó, nếu không thì trả về lỗi 500.
        return  ResponseEntity.status(status).body(APIResponse.response(status, exception.getMessage(), null));
        // Trả về JSON: {code: 400, messages: "...", result: null}.
    }



    // Bắt lỗi kiểm tra dữ liệu: @NotBlank, @Size, @Email, @Pattern...
    @ExceptionHandler(MethodArgumentNotValidException.class) // Khi field @NotBlank vi phạm, Spring tự throw lỗi này.
    // Cấu hình bắt exception của thư viện kiểm tra dữ liệu rồi trả về cho FE.
    public ResponseEntity<APIResponse<Object>> handleValidation(MethodArgumentNotValidException exception){ // Hàm xử lý lỗi kiểm tra dữ liệu.
        List<String> errors = exception
                .getBindingResult() // Lấy tất cả lỗi kiểm tra dữ liệu, ví dụ name rỗng hoặc phone sai.
                .getFieldErrors() // Lấy danh sách field bị lỗi.
                .stream()  // Chuyển list thành Stream để xử lý.
                .map( error -> {  // Duyệt từng lỗi.
                            String enumKey = error.getDefaultMessage(); // enumKey là thông báo đã set, ví dụ @NotBlank(message = "FIELD_REQUIRED").
                            // Tìm enum ErrorCode.FIELD_REQUIRED.
                            // Lấy .getMessage() tương ứng.
                            try {
                                return ErrorCode.valueOf(enumKey).getMessage();
                            } catch (IllegalArgumentException e) {
                                return enumKey;
                            }
                        }
                ).toList();
        // Trả về FE bằng ResponseEntity.
        // badRequest() là lỗi 400.
        // errors là tất cả mã lỗi đã được chuyển đổi.

        return ResponseEntity.badRequest().body(APIResponse.response(400, "Dữ liệu không hợp lệ", errors));
    }
}

