package com.duri.global.serializer;

import com.duri.global.util.AESUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class EncryptIdSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object id, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        try {
            if (id != null) {
                String strValue = id.toString();
                String encrypted = AESUtil.encrypt(strValue);
                gen.writeString(encrypted);
            }
        } catch (Exception e) {
            throw new RuntimeException("EncryptIdSerializer - Encryption Failed", e);
        }
    }
}
