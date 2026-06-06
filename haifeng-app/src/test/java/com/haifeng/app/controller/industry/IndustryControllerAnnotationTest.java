package com.haifeng.app.controller.industry;

import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndustryControllerAnnotationTest {

    @Test
    void enterprises_requiresOnlyPro() throws Exception {
        Method method = IndustryController.class.getMethod("enterprises", List.class);

        assertThat(method.getAnnotation(RequirePro.class)).isNotNull();
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }
}
