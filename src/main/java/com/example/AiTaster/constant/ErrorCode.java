package com.example.AiTaster.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor

public enum ErrorCode {
    FIELD_REQUIRED("Không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_SIZE("Độ dài phải từ 1 đến 50", HttpStatus.BAD_REQUEST),
    INVALID_FORMART("Định dạng không hợp lệ", HttpStatus.BAD_REQUEST),

    NOT_FOUND("Không tìm thấy", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("Không tìm thấy người dùng", HttpStatus.NOT_FOUND),

    DUPLICATE_EMAIL("Email đã tồn tại", HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE("Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN("Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    INVALID_ROLE("Vai trò không hợp lệ", HttpStatus.BAD_REQUEST),

    ACCOUNT_LOCKED("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    INVALID_LOGIN("Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST("Tài khoản đã tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("Mật khẩu là bắt buộc", HttpStatus.BAD_REQUEST),

    CALL_AI_FAILED("Gọi dịch vụ AI thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    BLOCKED_KEYWORD("Nội dung chứa từ khóa bị chặn", HttpStatus.BAD_REQUEST),
    PROMPT_INJECTION("Nội dung có dấu hiệu prompt injection", HttpStatus.BAD_REQUEST),
    PRICE_INVALID("Giá phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),

    APPLICATION_NOT_FOUND("Không tìm thấy hồ sơ ứng tuyển", HttpStatus.NOT_FOUND),
    CONVERSATION_NOT_FOUND("Không tìm thấy cuộc trò chuyện", HttpStatus.NOT_FOUND),
    CONVERSATION_ALREADY_EXISTS("Cuộc trò chuyện cho hồ sơ ứng tuyển này đã tồn tại", HttpStatus.CONFLICT),
    ONLY_CLIENT_CAN_START_CONVERSATION("Chỉ khách hàng mới có thể bắt đầu cuộc trò chuyện", HttpStatus.FORBIDDEN),
    NOT_APPLICATION_OWNER("Bạn không phải chủ sở hữu tin tuyển dụng của hồ sơ này", HttpStatus.FORBIDDEN),
    NOT_CONVERSATION_MEMBER("Bạn không phải thành viên của cuộc trò chuyện này", HttpStatus.FORBIDDEN),
    CLIENT_MUST_SEND_FIRST_MESSAGE("Khách hàng phải gửi tin nhắn đầu tiên", HttpStatus.FORBIDDEN),
    MESSAGE_NOT_FOUND("Không tìm thấy tin nhắn", HttpStatus.NOT_FOUND),
    MESSAGE_CONTENT_REQUIRED("Nội dung tin nhắn là bắt buộc", HttpStatus.BAD_REQUEST),
    FILE_URL_REQUIRED("Đường dẫn file là bắt buộc", HttpStatus.BAD_REQUEST),

    NOTIFICATION_NOT_FOUND("Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOT_NOTIFICATION_OWNER("Bạn không phải chủ sở hữu thông báo này", HttpStatus.FORBIDDEN),

    REPORT_NOT_FOUND("Không tìm thấy báo cáo", HttpStatus.NOT_FOUND),
    NOT_REPORT_OWNER("Bạn không phải chủ sở hữu báo cáo này", HttpStatus.FORBIDDEN),
    CANNOT_UPDATE_REPORT("Chỉ có thể cập nhật báo cáo đang chờ xử lý", HttpStatus.BAD_REQUEST),
    CANNOT_REPORT_YOURSELF("Bạn không thể báo cáo chính mình", HttpStatus.BAD_REQUEST),
    EVIDENCE_FILE_INVALID("File bằng chứng không hợp lệ", HttpStatus.BAD_REQUEST),

    AI_SERVICE_NOT_FOUND("Không tìm thấy dịch vụ AI", HttpStatus.NOT_FOUND),
    AI_SERVICE_ALREADY_DELETED("Dịch vụ AI đã bị xóa", HttpStatus.BAD_REQUEST),
    AI_SERVICE_ALREADY_OPEN("Dịch vụ AI đã được mở bán", HttpStatus.BAD_REQUEST),
    AI_SERVICE_PENDING_REVIEW("Dịch vụ AI đang chờ duyệt", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REJECTED("Chỉ có thể gửi lại dịch vụ AI đã bị từ chối", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REVIEWABLE("Chỉ có thể duyệt dịch vụ AI ở trạng thái nháp hoặc đang chờ duyệt", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_PUBLIC("Dịch vụ AI chưa được công khai", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_READY_FOR_REVIEW("Dịch vụ AI chưa sẵn sàng để duyệt", HttpStatus.BAD_REQUEST),
    ONLY_ADMIN_CAN_REVIEW_AI_SERVICE("Chỉ quản trị viên mới có thể duyệt dịch vụ AI", HttpStatus.FORBIDDEN),
    REJECTION_REASON_REQUIRED("Lý do từ chối là bắt buộc", HttpStatus.BAD_REQUEST),
    SERVICE_FEE_INVALID("Phí dịch vụ phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    SERVICE_SKILL_REQUIRED("Kỹ năng là bắt buộc", HttpStatus.BAD_REQUEST),
    SERVICE_CATEGORY_REQUIRED("Danh mục là bắt buộc", HttpStatus.BAD_REQUEST),
    SERVICE_FILE_REQUIRED("Cần có file tài liệu và file mã nguồn trước khi gửi duyệt", HttpStatus.BAD_REQUEST),
    //---------------------------------------------------------------------------
    INVALID_REFRESH_TOKEN("Token làm mới không hợp lệ!", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("Token đã hết hạn!", HttpStatus.UNAUTHORIZED);


    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
