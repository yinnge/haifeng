package com.haifeng.app.service.employment.jobIndex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;

public interface JobIndexService {

    IPage<JobIndexListVO> page(JobSearchDTO dto);

    JobIndexDetailVO detail(Long id);
}
