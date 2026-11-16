package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;
import com.haifeng.admin.vo.home.InstitutionOptionVO;
import com.haifeng.admin.vo.home.TeacherOptionVO;

import java.util.List;

public interface InstitutionService {

    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);

    InstitutionDetailVO detail(Long id);

    Long add(InstitutionAddDTO dto);

    void update(Long id, InstitutionUpdateDTO dto);

    void updateStatus(Long id, StatusDTO dto);

    void delete(Long id);

    /**
     * 分类下拉选项（写死的 4 个值）
     */
    List<InstitutionOptionVO> categories();

    /**
     * 教师身份会员下拉选项（id + username），供「管理账号」选择用
     */
    List<TeacherOptionVO> listTeacherOptions();
}
