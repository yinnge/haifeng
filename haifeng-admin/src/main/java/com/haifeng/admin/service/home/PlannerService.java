package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
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

    /**
     * 修改上下架状态。
     *
     * <p>规划师无 status 列，状态由软删除标记 is_deleted 承担：
     * {@code 1} = 上线（is_deleted=false），{@code 0} = 下线（is_deleted=true）。
     */
    void updateStatus(Long id, StatusDTO dto);

    /** 彻底删除规划师（物理 DELETE，不可恢复） */
    void hardDelete(Long id);
}
