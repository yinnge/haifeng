package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommunityPositionService {
    IPage<CommunityPositionListVO> page(CommunityPositionQueryDTO dto);
    CommunityPositionDetailVO detail(Long id);
    void update(Long id, CommunityPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
