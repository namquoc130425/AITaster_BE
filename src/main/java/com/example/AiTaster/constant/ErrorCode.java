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
    FIELD_REQUIRED("Trường bắt buộc không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_SIZE("Độ dài phải từ 1 đến 50 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_FORMART("Định dạng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("Định dạng không hợp lệ", HttpStatus.BAD_REQUEST),
    TITLE_REQUIRED("Tiêu đề dự án không được để trống", HttpStatus.BAD_REQUEST),
    TITLE_MIN_LENGTH("Tiêu đề dự án phải có ít nhất 20 ký tự", HttpStatus.BAD_REQUEST),
    BUDGETS_INVALID("Ngân sách phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    TIMELINE_VALUE_INVALID("Thời gian thực hiện phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    CLIENT_TERMS_REQUIRED("Bạn phải đồng ý với điều khoản dành cho khách hàng", HttpStatus.BAD_REQUEST),
    EXPERT_TERMS_REQUIRED("Bạn phải đồng ý với điều khoản dành cho chuyên gia", HttpStatus.BAD_REQUEST),

    NOT_FOUND("Không tìm thấy dữ liệu", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("Không tìm thấy người dùng", HttpStatus.NOT_FOUND),

    DUPLICATE_EMAIL("Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE("Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN("Mã xác thực không hợp lệ", HttpStatus.UNAUTHORIZED),
    INVALID_ROLE("Vai trò người dùng không hợp lệ", HttpStatus.BAD_REQUEST),

    ACCOUNT_LOCKED("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    INVALID_LOGIN("Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST("Tài khoản đã tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),

    CALL_AI_FAILED("Không thể kết nối với dịch vụ AI", HttpStatus.INTERNAL_SERVER_ERROR),

    BLOCKED_KEYWORD("Nội dung chứa từ khóa không được phép", HttpStatus.BAD_REQUEST),
    PROMPT_INJECTION("Nội dung có dấu hiệu can thiệp câu lệnh AI", HttpStatus.BAD_REQUEST),
    PRICE_INVALID("Giá phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),
    PROJECT_PRICE_MINIMUM("Giá toàn bộ dự án phải từ 10.000 VND trở lên", HttpStatus.BAD_REQUEST),

    APPLICATION_NOT_FOUND("Không tìm thấy hồ sơ ứng tuyển", HttpStatus.NOT_FOUND),
    JOB_POST_NOT_FOUND("Không tìm thấy bài đăng dự án", HttpStatus.NOT_FOUND),
    CONVERSATION_NOT_FOUND("Không tìm thấy cuộc trò chuyện", HttpStatus.NOT_FOUND),
    CONVERSATION_ALREADY_EXISTS("Hồ sơ ứng tuyển này đã có cuộc trò chuyện", HttpStatus.CONFLICT),
    ONLY_CLIENT_CAN_START_CONVERSATION("Chỉ khách hàng mới có thể bắt đầu cuộc trò chuyện", HttpStatus.FORBIDDEN),
    NOT_APPLICATION_OWNER("Bạn không sở hữu bài đăng của hồ sơ ứng tuyển này", HttpStatus.FORBIDDEN),
    NOT_CONVERSATION_MEMBER("Bạn không phải thành viên của cuộc trò chuyện này", HttpStatus.FORBIDDEN),
    CLIENT_MUST_SEND_FIRST_MESSAGE("Khách hàng phải gửi tin nhắn đầu tiên", HttpStatus.FORBIDDEN),
    MESSAGE_NOT_FOUND("Không tìm thấy tin nhắn", HttpStatus.NOT_FOUND),
    MESSAGE_CONTENT_REQUIRED("Nội dung tin nhắn không được để trống", HttpStatus.BAD_REQUEST),
    FILE_URL_REQUIRED("Đường dẫn tệp không được để trống", HttpStatus.BAD_REQUEST),

    NOTIFICATION_NOT_FOUND("Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOT_NOTIFICATION_OWNER("Bạn không sở hữu thông báo này", HttpStatus.FORBIDDEN),

    REPORT_NOT_FOUND("Không tìm thấy báo cáo", HttpStatus.NOT_FOUND),
    NOT_REPORT_OWNER("Bạn không sở hữu báo cáo này", HttpStatus.FORBIDDEN),
    CANNOT_UPDATE_REPORT("Chỉ có thể cập nhật báo cáo đang chờ xử lý", HttpStatus.BAD_REQUEST),
    CANNOT_REPORT_YOURSELF("Bạn không thể tự báo cáo chính mình", HttpStatus.BAD_REQUEST),
    EVIDENCE_FILE_INVALID("Tệp bằng chứng không hợp lệ", HttpStatus.BAD_REQUEST),

    AI_SERVICE_NOT_FOUND("Không tìm thấy dịch vụ AI", HttpStatus.NOT_FOUND),
    AI_SERVICE_ALREADY_DELETED("Dịch vụ AI đã bị xóa", HttpStatus.BAD_REQUEST),
    AI_SERVICE_ALREADY_OPEN("Dịch vụ AI đang được mở", HttpStatus.BAD_REQUEST),
    AI_SERVICE_PENDING_REVIEW("Dịch vụ AI đang chờ duyệt", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REJECTED("Chỉ dịch vụ AI bị từ chối mới có thể gửi duyệt lại", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REVIEWABLE("Chỉ có thể duyệt dịch vụ AI ở trạng thái nháp hoặc chờ duyệt", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_PUBLIC("Dịch vụ AI chưa được công khai", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_READY_FOR_REVIEW("Dịch vụ AI chưa đủ điều kiện để gửi duyệt", HttpStatus.BAD_REQUEST),
    ONLY_ADMIN_CAN_REVIEW_AI_SERVICE("Chỉ quản trị viên mới có thể duyệt dịch vụ AI", HttpStatus.FORBIDDEN),
    REJECTION_REASON_REQUIRED("Lý do từ chối không được để trống", HttpStatus.BAD_REQUEST),
    SERVICE_FEE_INVALID("Phí dịch vụ AI phải từ 10.000 VND trở lên", HttpStatus.BAD_REQUEST),
    SERVICE_SKILL_REQUIRED("Vui lòng chọn ít nhất một kỹ năng", HttpStatus.BAD_REQUEST),
    SERVICE_CATEGORY_REQUIRED("Vui lòng chọn danh mục", HttpStatus.BAD_REQUEST),
    SERVICE_FILE_REQUIRED("Phải có tệp tài liệu và mã nguồn trước khi gửi duyệt", HttpStatus.BAD_REQUEST),
    RATING_INVALID("Số sao đánh giá phải là số nguyên từ 1 đến 5", HttpStatus.BAD_REQUEST),
    REVIEW_INVALID_SIZE("Nội dung đánh giá không được vượt quá 2.000 ký tự", HttpStatus.BAD_REQUEST),

    SUPABASE_TOKEN_INVALID("Mã xác thực Supabase không hợp lệ", HttpStatus.UNAUTHORIZED),
    SUPABASE_ACCOUNT_NOT_AUTHENTICATED("Tài khoản Supabase chưa được xác thực", HttpStatus.UNAUTHORIZED),
    SUPABASE_GOOGLE_PROVIDER_REQUIRED("Tài khoản đăng nhập phải được xác thực qua Google", HttpStatus.UNAUTHORIZED),
    SUPABASE_EMAIL_REQUIRED("Không tìm thấy email từ tài khoản Supabase", HttpStatus.BAD_REQUEST),
    SUPABASE_ACCOUNT_NOT_REGISTERED("Tài khoản Supabase chưa được đăng ký trên AITasker", HttpStatus.NOT_FOUND),
    SUPABASE_ACCOUNT_ALREADY_LINKED("Tài khoản Supabase này đã được liên kết với người dùng khác", HttpStatus.CONFLICT),
    //---------------------------------------------------------------------------
    INVALID_REFRESH_TOKEN("Mã làm mới phiên đăng nhập không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_JOB_POST_INPUT("Nội dung bài đăng dự án không hợp lệ", HttpStatus.BAD_REQUEST),;


    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
