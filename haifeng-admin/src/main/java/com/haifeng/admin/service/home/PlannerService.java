package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;
import com.haifeng.admin.vo.home.TeacherOptionVO;

import java.util.List;

public interface PlannerService {

    /** 教师身份用户下拉选项（弹窗展示用户名、回传 userId） */
    List<TeacherOptionVO> listTeacherOptions();

    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    PlannerDetailVO detail(Long id);

    Long add(PlannerAddDTO dto);

    void update(Long id, PlannerUpdateDTO dto);

    /** 软删除规划师（置 is_deleted=true，非物理删除） */
    void delete(Long id);
}
