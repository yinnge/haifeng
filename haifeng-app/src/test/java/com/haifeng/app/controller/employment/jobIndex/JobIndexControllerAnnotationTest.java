package com.haifeng.app.controller.employment.jobIndex;

import com.haifeng.common.annotation.RequireLogin;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class JobIndexControllerAnnotationTest {

    @Test
    void listMethod_shouldBePublic() throws Exception {
        Method method = JobIndexController.class.getMethod("list", com.haifeng.app.dto.employment.jobIndex.JobSearchDTO.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/list");
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }

    @Test
    void detailMethod_shouldRequireLogin() throws Exception {
        Method method = JobIndexController.class.getMethod("detail", Long.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/{id}/detail");
        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
    }

    @Test
    void controller_shouldHaveRestControllerAndRequestMapping() {
        RestController restCtrl = JobIndexController.class.getAnnotation(RestController.class);
        assertThat(restCtrl).isNotNull();
        RequestMapping mapping = JobIndexController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/api/v1/app/employment/job");
    }
}
