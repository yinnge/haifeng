package com.haifeng.app.controller.company;

import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseControllerAnnotationTest {

    @Test
    void positions_requiresLogin() throws Exception {
        Method method = EnterpriseController.class.getMethod("positions", Long.class);

        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
        assertThat(method.getAnnotation(RequirePro.class)).isNull();
    }

    @Test
    void industries_requiresOnlyPro() throws Exception {
        Method method = EnterpriseController.class.getMethod("industries", List.class);

        assertThat(method.getAnnotation(RequirePro.class)).isNotNull();
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }
}
