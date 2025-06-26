package com.duri.global.serializer;

import com.duri.global.annotation.EncryptId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.lang.reflect.Field;
import java.util.List;
import org.springframework.util.ReflectionUtils;

public class EncryptIdBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
        BeanDescription beanDesc,
        List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            Field field = ReflectionUtils.findField(beanDesc.getBeanClass(), writer.getName());
            if (field != null && field.isAnnotationPresent(EncryptId.class)) {
                writer.assignSerializer(new EncryptIdSerializer());
            }
        }
        return beanProperties;
    }
}
