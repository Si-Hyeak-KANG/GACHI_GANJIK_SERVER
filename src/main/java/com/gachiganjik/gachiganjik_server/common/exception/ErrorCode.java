package com.gachiganjik.gachiganjik_server.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // 인증
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    GUEST_NOT_ALLOWED(HttpStatus.FORBIDDEN, "GUEST_NOT_ALLOWED", "게스트는 이 기능을 사용할 수 없습니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    WEAK_PASSWORD(HttpStatus.UNPROCESSABLE_ENTITY, "WEAK_PASSWORD", "비밀번호 정책을 충족하지 않습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 파일 형식입니다."),

    // 앨범
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALBUM_NOT_FOUND", "앨범을 찾을 수 없습니다."),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_CODE_NOT_FOUND", "유효하지 않은 초대 코드입니다."),
    ALREADY_ALBUM_MEMBER(HttpStatus.CONFLICT, "ALREADY_ALBUM_MEMBER", "이미 앨범 멤버입니다."),
    NOT_ALBUM_MEMBER(HttpStatus.FORBIDDEN, "NOT_ALBUM_MEMBER", "앨범 멤버가 아닙니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "PERMISSION_DENIED", "권한이 없습니다."),
    ALBUM_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "ALBUM_LIMIT_EXCEEDED", "앨범은 최대 8개까지 생성할 수 있습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "종료일은 시작일 이후여야 합니다."),
    INVALID_INVITE_CODE(HttpStatus.NOT_FOUND, "INVALID_INVITE_CODE", "유효하지 않은 초대 코드입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "ALREADY_JOINED", "이미 참여한 앨범입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "앨범 멤버를 찾을 수 없습니다."),
    CANNOT_MANAGE_OWNER(HttpStatus.FORBIDDEN, "CANNOT_MANAGE_OWNER", "OWNER는 관리 대상이 될 수 없습니다."),
    OWNER_CANNOT_WITHDRAW(HttpStatus.FORBIDDEN, "OWNER_CANNOT_WITHDRAW", "OWNER인 앨범이 있어 탈퇴할 수 없습니다. 소유권을 이전한 뒤 다시 시도해 주세요."),

    // GUEST
    GUEST_KEY_ALREADY_EXISTS(HttpStatus.CONFLICT, "GUEST_KEY_ALREADY_EXISTS", "이미 사용 중인 GUEST ID입니다."),
    GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "GUEST 정보를 찾을 수 없습니다."),
    GUEST_ALREADY_CONVERTED(HttpStatus.GONE, "GUEST_ALREADY_CONVERTED", "이미 회원 전환된 GUEST ID입니다."),

    // 사진
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND", "사진을 찾을 수 없습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_SIZE_EXCEEDED", "파일 크기 제한을 초과했습니다."),
    TOO_MANY_FILES(HttpStatus.BAD_REQUEST, "TOO_MANY_FILES", "한 번에 최대 10장까지 업로드할 수 있습니다."),
    NOT_PHOTO_OWNER(HttpStatus.FORBIDDEN, "NOT_PHOTO_OWNER", "사진 업로더만 수정할 수 있습니다."),

    // 이메일 인증
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_VERIFICATION_NOT_FOUND", "인증 코드가 존재하지 않거나 만료되었습니다."),
    EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_INVALID", "인증 코드가 올바르지 않습니다."),

    // 소셜 로그인
    SIGNUP_TICKET_NOT_FOUND(HttpStatus.UNAUTHORIZED, "SIGNUP_TICKET_NOT_FOUND", "가입 정보가 만료되었습니다. 다시 시도해 주세요."),
    SOCIAL_PROVIDER_CONFLICT(HttpStatus.CONFLICT, "SOCIAL_PROVIDER_CONFLICT", "해당 번호로 이미 연결된 소셜 계정이 있습니다."),
    LINK_TICKET_NOT_FOUND(HttpStatus.UNAUTHORIZED, "LINK_TICKET_NOT_FOUND", "연결 정보가 만료되었습니다. 다시 시도해 주세요."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}