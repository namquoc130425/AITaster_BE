package com.example.AiTaster.exception;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
@ControllerAdvice  // áp dụng cho tất cả controller trong hệ thống
@Slf4j
public class GlobalExceptionHander {
    // Bắt lỗi business do mình tự throw
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<APIResponse <Object>> handlerGlobal(GlobalException exception) {
        int status = (exception.getCode() != null)
                ? exception.getCode()
                : HttpStatus.INTERNAL_SERVER_ERROR.value(); // nếu có code thì lấy code , nếu không có code thì trả về lỗi 500 , exception.getcode là code mình tự set ví dụ 400,500 . nếu null thì dùng 500
        return  ResponseEntity.status(status).body(APIResponse.response(status, exception.getMessage(), null));
        // trả về JSON : {code : 400 , messages:"Duplicate PHONE,v.v.", result : null}
    }



    // Bắt lỗi validation: @NotBlank, @Size, @Email, @Pattern...
    @ExceptionHandler(MethodArgumentNotValidException.class) // khi filed @NotBlank vi phạm -> Spring throw cái này tự động
    // cấu hình bắt exception của thư vienj validation rồi trả về cho fe
    public ResponseEntity<APIResponse<Object>> handleValidation(MethodArgumentNotValidException exception){ // hàm bắt lỗi tự tạo -> nếu thuộc lỗi loại GlobalException -> gọi hàm này đễ xữ lý  >
        List<String> errors = exception
                .getBindingResult() // lấy tất cả lỗi validation      ví dụ : name rổng , phone sai
                .getFieldErrors() // lấy dánh sách Filed bị lỗi
                .stream()  // chuyển list thành Stream đễ xú lý
                .map( error -> {  // duyệt từng lõi
                            String enumKey = error.getDefaultMessage(); // enumKey là cái message mình đã set trong @NotBlank(message = "FIELD_REQUIRED") , lấy ra để chuyển thành enum
                            ErrorCode errorCode;
                            //tìm enum ErrorCode.FIELD_REQUIRED
                            //lấy .getMessage() = "Cannot be blank"
                            try {
                                errorCode = ErrorCode.valueOf(enumKey);
                            } catch (IllegalArgumentException e) {
                                errorCode = ErrorCode.FIELD_REQUIRED;

                            }
                            return  error.getField() + "" +  errorCode.getMessage(); // → "userName Cannot be blank"
                        }
                ).toList();
        // trả về FE bắt buột phải dùng ResponseEntity
        // badRequest() là lỗi 400
        //APIResonse không trả về phía FE
        // errors là tất các mã lỗi đã đc chuyển đổi

        return ResponseEntity.badRequest().body(APIResponse.response(400, "Validation Failed", errors)); // → { code: 400, messages: "Validation Failed", result: ["userName Cannot be blank"] }
    }
}

