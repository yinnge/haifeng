package com.haifeng.admin.service.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.city.CityAddDTO;
import com.haifeng.admin.dto.city.CityDetailUpdateDTO;
import com.haifeng.admin.dto.city.CityQueryDTO;
import com.haifeng.admin.dto.city.CityStatusDTO;
import com.haifeng.admin.dto.city.CityUpdateDTO;
import com.haifeng.admin.vo.city.CityDetailVO;
import com.haifeng.admin.vo.city.CityListVO;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CityService {

    /**
     * 分页查询城市列表
     */
    IPage<CityListVO> page(CityQueryDTO dto);

    /**
     * 获取城市详情（主表+详情表）
     */
    CityDetailVO detail(Long id);

    /**
     * 新增城市（事务：主表+详情一起创建）
     */
    Long add(CityAddDTO dto);

    /**
     * 更新城市主表信息
     */
    void update(Long id, CityUpdateDTO dto);

    /**
     * 更新城市详情表信息
     */
    void updateDetail(Long id, CityDetailUpdateDTO dto);

    /**
     * 更新城市状态（禁用/启用）
     */
    void updateStatus(Long id, CityStatusDTO dto);

    /**
     * 硬删除城市（主表+详情表）
     */
    void delete(Long id);

    /**
     * 批量硬删除城市
     */
    void batchDelete(List<Long> ids);

    /**
     * 导入城市主表xlsx
     */
    void importCities(MultipartFile file);

    /**
     * 导入城市详情xlsx（多Sheet）
     */
    void importCityDetails(MultipartFile file);
}
