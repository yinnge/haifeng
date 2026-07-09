package com.haifeng.admin.service.impl.company;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.excel.company.EnterpriseIndustryExcelDTO;
import com.haifeng.admin.service.company.EnterpriseIndustryService;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseIndustryServiceImpl implements EnterpriseIndustryService {

    private final EnterpriseIndustryMapper enterpriseIndustryMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final IndustryMapper industryMapper;

    @Override
    public IPage<EnterpriseIndustryListVO> page(EnterpriseIndustryQueryDTO dto) {
        Page<EnterpriseIndustry> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<>();

        // 企业名称模糊查询
        if (StringUtils.hasText(dto.getEnterpriseName())) {
            wrapper.like(EnterpriseIndustry::getEnterpriseName, dto.getEnterpriseName());
        }
        // 行业名称模糊查询
        if (StringUtils.hasText(dto.getIndustryName())) {
            wrapper.like(EnterpriseIndustry::getIndustryName, dto.getIndustryName());
        }

        // 按创建时间降序
        wrapper.orderByDesc(EnterpriseIndustry::getCreatedAt);

        IPage<EnterpriseIndustry> enterpriseIndustryPage = enterpriseIndustryMapper.selectPage(page, wrapper);

        return enterpriseIndustryPage.convert(entity -> {
            EnterpriseIndustryListVO vo = new EnterpriseIndustryListVO();
            BeanUtils.copyProperties(entity, vo);
            // 处理时间类型转换
            if (entity.getCreatedAt() != null) {
                vo.setCreatedAt(entity.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public EnterpriseIndustryDetailVO detail(Long id) {
        EnterpriseIndustry entity = enterpriseIndustryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "企业行业关联记录不存在");
        }

        EnterpriseIndustryDetailVO vo = new EnterpriseIndustryDetailVO();
        BeanUtils.copyProperties(entity, vo);

        // 处理时间类型转换
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(entity.getCreatedAt().toLocalDateTime());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        EnterpriseIndustry entity = enterpriseIndustryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "企业行业关联记录不存在");
        }

        enterpriseIndustryMapper.deleteById(id);

        log.info("硬删除企业行业关联成功: id={}, enterpriseName={}, industryName={}",
                id, entity.getEnterpriseName(), entity.getIndustryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }

        int deleted = enterpriseIndustryMapper.deleteBatchIds(ids);

        log.info("批量硬删除企业行业关联成功: 删除数量={}, ids={}", deleted, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importEnterpriseIndustries(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0读取企业行业关联数据
            List<EnterpriseIndustryExcelDTO> excelData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseIndustryExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            if (excelData == null || excelData.isEmpty()) {
                throw new BusinessException(400, "导入失败：数据Sheet为空");
            }

            // 用于检查文件内(enterpriseId, industryId)重复
            Set<String> pairsInFile = new HashSet<>();
            // 待插入的记录列表
            List<EnterpriseIndustry> records = new ArrayList<>();

            OffsetDateTime now = OffsetDateTime.now();

            for (int i = 0; i < excelData.size(); i++) {
                int rowNum = i + 2;
                EnterpriseIndustryExcelDTO dto = excelData.get(i);

                // 校验企业名称必填
                if (!StringUtils.hasText(dto.getEnterpriseName())) {
                    errorMsgs.add("第" + rowNum + "行：企业名称不能为空");
                    continue;
                }

                // 校验行业名称必填
                if (!StringUtils.hasText(dto.getIndustryName())) {
                    errorMsgs.add("第" + rowNum + "行：行业名称不能为空");
                    continue;
                }

                // 校验企业名称存在于t_enterprise
                Long enterpriseId = enterpriseMapper.findIdByEnterpriseName(dto.getEnterpriseName());
                if (enterpriseId == null) {
                    errorMsgs.add("第" + rowNum + "行：企业名称'" + dto.getEnterpriseName() + "'不存在");
                    continue;
                }

                // 校验行业名称存在于t_industry
                LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Industry::getIndustryName, dto.getIndustryName())
                       .eq(Industry::getIsDeleted, false);
                Industry industry = industryMapper.selectOne(wrapper);
                if (industry == null) {
                    errorMsgs.add("第" + rowNum + "行：行业名称'" + dto.getIndustryName() + "'不存在");
                    continue;
                }
                Long industryId = industry.getId();

                // 检查文件内(enterpriseId, industryId)重复
                String pairKey = enterpriseId + "_" + industryId;
                if (pairsInFile.contains(pairKey)) {
                    errorMsgs.add("第" + rowNum + "行：企业'" + dto.getEnterpriseName()
                            + "'与行业'" + dto.getIndustryName() + "'的关联在文件中重复");
                    continue;
                }
                pairsInFile.add(pairKey);

                // 检查数据库中(enterpriseId, industryId)是否已存在
                if (enterpriseIndustryMapper.existsByEnterpriseIdAndIndustryId(enterpriseId, industryId)) {
                    errorMsgs.add("第" + rowNum + "行：企业'" + dto.getEnterpriseName()
                            + "'与行业'" + dto.getIndustryName() + "'的关联已存在于数据库中");
                    continue;
                }

                Long id = SnowflakeIdGenerator.nextId();

                EnterpriseIndustry entity = EnterpriseIndustry.builder()
                        .id(id)
                        .enterpriseId(enterpriseId)
                        .enterpriseName(dto.getEnterpriseName())
                        .industryId(industryId)
                        .industryName(dto.getIndustryName())
                        .isPrimary(false)
                        .sortOrder((short) 0)
                        .createdAt(now)
                        .build();

                records.add(entity);
            }

            // 如果有错误，抛出异常
            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 批量插入
            if (!records.isEmpty()) {
                enterpriseIndustryMapper.insertBatch(records);
            }
            log.info("导入企业行业关联成功，数量={}", records.size());

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("导入企业行业关联数据失败", e);
            throw new BusinessException(500, "导入企业行业关联数据失败：" + e.getMessage());
        }
    }
}
