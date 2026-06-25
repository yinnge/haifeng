package com.haifeng.app.controller.city;

import com.haifeng.common.annotation.RequireLogin;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class CityControllerAnnotationTest {

    @Test
    void idByName_requiresLoginAndUsesIdPath() throws Exception {
        Method method = CityController.class.getMethod("idByName", String.class);

        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        assertThat(getMapping).isNotNull();
        assertThat(getMapping.value()).containsExactly("/id");
    }
}
