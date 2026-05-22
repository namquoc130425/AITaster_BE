package com.example.AiTaster.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)  // fill nào bằng null thì ko trả về phía frontend
@Builder
public class APIResponse<T> {
    int code;
    String messages;
    T result;

    public static <T> APIResponse response(int code, String messages, T result) {
        return APIResponse.<T>builder()
                .code(code)
                .messages(messages)
                .result(result)
                .build();
    }
}
