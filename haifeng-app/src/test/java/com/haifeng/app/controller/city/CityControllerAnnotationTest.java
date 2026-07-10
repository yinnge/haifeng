package com.haifeng.app.controller.city;

import com.haifeng.common.annotation.RequireLogin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class CityControllerAnnotationTest {

    @Test
    void detail_requiresLoginAndUsesPathVariable() throws Exception {
        Method method = CityController.class.getMethod("detail", Long.class);

        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
    }

    @Test
    void list_isPublic() throws Exception {
        Method method = CityController.class.getMethod("list", com.haifeng.app.dto.city.CityQueryDTO.class);

        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }
}
