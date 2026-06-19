package com.haifeng.app.controller.employment.jobIndex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.employment.jobIndex.JobIndexService;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/job")
@RequiredArgsConstructor
public class JobIndexController {

    private final JobIndexService jobIndexService;

    @GetMapping("/list")
    public R<IPage<JobIndexListVO>> list(@Valid JobSearchDTO dto) {
        return R.ok(jobIndexService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<JobIndexDetailVO> detail(@PathVariable Long id) {
        return R.ok(jobIndexService.detail(id));
    }
}
