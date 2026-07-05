package com.haifeng.admin.service.employment.contentManagement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeUpdateDTO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeService {
    IPage<NoticeListVO> page(NoticeQueryDTO dto);
    NoticeDetailVO detail(Long id);
    void update(Long id, NoticeUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
