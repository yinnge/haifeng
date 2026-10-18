package com.haifeng.admin.service.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.fileload.BizArticleAddDTO;
import com.haifeng.admin.dto.fileload.BizArticleQueryDTO;
import com.haifeng.admin.dto.fileload.BizArticleUpdateDTO;
import com.haifeng.admin.vo.fileload.BizArticleDetailVO;
import com.haifeng.admin.vo.fileload.BizArticleListVO;

import java.util.List;

/**
 * 热点资讯 Service（高考/考公/考研/中考/英语四六级五模块共用）
 */
public interface BizArticleService {

    /** 分页查询：年份/省份/模块名/专栏名精准 + 标题模糊 */
    IPage<BizArticleListVO> page(BizArticleQueryDTO dto);

    /** 详情（含富文本正文），访问即浏览量 +1 */
    BizArticleDetailVO detail(Long id);

    /** 查询指定模块下已填写过的专栏名（去重），供前端下拉选择 */
    List<String> listColumns(String moduleName);

    /** 新增，返回雪花 id */
    Long add(BizArticleAddDTO dto);

    /** 修改 */
    void update(Long id, BizArticleUpdateDTO dto);

    /** 删除（表无逻辑删除列，物理删除） */
    void delete(Long id);
}
