package com.gachiganjik.gachiganjik_server.domain.user.service;

/**
 * 연결 확인 화면에 보여줄 이메일 마스킹. 예: admin@mowa.com → a****@mowa.com
 */
public final class EmailMasker {

    private EmailMasker() {
    }

    public static String mask(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }

        int at = email.indexOf('@');
        if (at <= 0) {
            return "****";
        }

        String local = email.substring(0, at);
        String domain = email.substring(at);

        if (local.length() == 1) {
            return local + "****" + domain;
        }
        return local.charAt(0) + "****" + domain;
    }
}