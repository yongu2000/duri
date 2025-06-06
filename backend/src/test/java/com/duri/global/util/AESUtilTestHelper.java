package com.duri.global.util;

import java.lang.reflect.Field;

public class AESUtilTestHelper {

    public static void setSecretKey(String key) {
        // 내부적으로 AESUtil의 static 필드를 세팅
        try {
            Field field = AESUtil.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(null, key); // static 필드니까 null 대상
        } catch (Exception e) {
            throw new RuntimeException("Failed to set AESUtil.secretKey", e);
        }
    }
}