package com.duri.global.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AESUtilTest {

    @Test
    void testUtil() throws Exception {
        long testLong = 1L;
        String testString = Long.toString(testLong);
        String encrypt = AESUtil.encrypt(testString);
        String decrypt = AESUtil.decrypt(encrypt);

        Assertions.assertThat(testLong).isEqualTo(Long.parseLong(decrypt));

    }
}