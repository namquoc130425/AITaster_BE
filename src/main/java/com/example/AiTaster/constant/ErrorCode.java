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
    FIELD_REQUIRED("Cannot be blank", HttpStatus.BAD_REQUEST),
    INVALID_SIZE("Must be between 1 and 50", HttpStatus.BAD_REQUEST),
    INVALID_FORMART("Invalid format", HttpStatus.BAD_REQUEST),

    NOT_FOUND("Not Found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),

    DUPLICATE_EMAIL("Duplicate email", HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE("Duplicate phone number", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN("Invalid Token", HttpStatus.UNAUTHORIZED),
    INVALID_ROLE("Invalid Role", HttpStatus.BAD_REQUEST),

    ACCOUNT_LOCKED("Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("Account is disabled", HttpStatus.FORBIDDEN),
    INVALID_LOGIN("Invalid name or password", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST("Account already exist", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("Password is required", HttpStatus.BAD_REQUEST),

    CALL_AI_FAILED("Call AI service failed", HttpStatus.INTERNAL_SERVER_ERROR),

    BLOCKED_KEYWORD("Input contains blocked keyword", HttpStatus.BAD_REQUEST),
    PROMPT_INJECTION("Input contains potential prompt injection patterns", HttpStatus.BAD_REQUEST),
    PRICE_INVALID("Price must be greater than or equal to 0", HttpStatus.BAD_REQUEST),

    APPLICATION_NOT_FOUND("Application not found", HttpStatus.NOT_FOUND),
    CONVERSATION_NOT_FOUND("Conversation not found", HttpStatus.NOT_FOUND),
    CONVERSATION_ALREADY_EXISTS("Conversation already exists for this application", HttpStatus.CONFLICT),
    ONLY_CLIENT_CAN_START_CONVERSATION("Only client can start a conversation", HttpStatus.FORBIDDEN),
    NOT_APPLICATION_OWNER("You are not owner of this application job post", HttpStatus.FORBIDDEN),
    NOT_CONVERSATION_MEMBER("You are not a member of this conversation", HttpStatus.FORBIDDEN),
    CLIENT_MUST_SEND_FIRST_MESSAGE("Client must send the first message", HttpStatus.FORBIDDEN),
    MESSAGE_NOT_FOUND("Message not found", HttpStatus.NOT_FOUND),
    MESSAGE_CONTENT_REQUIRED("Message content is required", HttpStatus.BAD_REQUEST),
    FILE_URL_REQUIRED("File URL is required", HttpStatus.BAD_REQUEST),

    NOTIFICATION_NOT_FOUND("Notification not found", HttpStatus.NOT_FOUND),
    NOT_NOTIFICATION_OWNER("You are not owner of this notification", HttpStatus.FORBIDDEN),

    REPORT_NOT_FOUND("Report not found", HttpStatus.NOT_FOUND),
    NOT_REPORT_OWNER("You are not owner of this report", HttpStatus.FORBIDDEN),
    CANNOT_UPDATE_REPORT("Only pending report can be updated", HttpStatus.BAD_REQUEST),
    CANNOT_REPORT_YOURSELF("You cannot report yourself", HttpStatus.BAD_REQUEST),
    EVIDENCE_FILE_INVALID("Evidence file invalid", HttpStatus.BAD_REQUEST),

    AI_SERVICE_NOT_FOUND("AI service not found", HttpStatus.NOT_FOUND),
    AI_SERVICE_ALREADY_DELETED("AI service already deleted", HttpStatus.BAD_REQUEST),
    AI_SERVICE_ALREADY_OPEN("AI service is already open", HttpStatus.BAD_REQUEST),
    AI_SERVICE_PENDING_REVIEW("AI service is pending review", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REJECTED("Only rejected AI service can be resubmitted", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_REVIEWABLE("Only draft or pending review AI service can be reviewed", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_PUBLIC("AI service is not public", HttpStatus.BAD_REQUEST),
    AI_SERVICE_NOT_READY_FOR_REVIEW("AI service is not ready for review", HttpStatus.BAD_REQUEST),
    ONLY_ADMIN_CAN_REVIEW_AI_SERVICE("Only admin can review AI service", HttpStatus.FORBIDDEN),
    REJECTION_REASON_REQUIRED("Rejection reason is required", HttpStatus.BAD_REQUEST),
    SERVICE_FEE_INVALID("Service fee must be greater than zero", HttpStatus.BAD_REQUEST),
    SERVICE_SKILL_REQUIRED("Skill is required", HttpStatus.BAD_REQUEST),
    SERVICE_CATEGORY_REQUIRED("Category is required", HttpStatus.BAD_REQUEST),
    SERVICE_FILE_REQUIRED("Document file and source file are required before review", HttpStatus.BAD_REQUEST),

    SUPABASE_TOKEN_INVALID("Invalid Supabase token", HttpStatus.UNAUTHORIZED),
    SUPABASE_ACCOUNT_NOT_AUTHENTICATED("Supabase account is not authenticated", HttpStatus.UNAUTHORIZED),
    SUPABASE_GOOGLE_PROVIDER_REQUIRED("Supabase token is not from Google provider", HttpStatus.UNAUTHORIZED),
    SUPABASE_EMAIL_REQUIRED("Supabase email is required", HttpStatus.BAD_REQUEST),
    SUPABASE_ACCOUNT_NOT_REGISTERED("Supabase account is not registered in AiTaster", HttpStatus.NOT_FOUND),
    SUPABASE_ACCOUNT_ALREADY_LINKED("This Supabase account is already linked to another user", HttpStatus.CONFLICT),
    //---------------------------------------------------------------------------
    INVALID_REFRESH_TOKEN("Invalid refresh token!", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("Expired token!", HttpStatus.UNAUTHORIZED);


    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
