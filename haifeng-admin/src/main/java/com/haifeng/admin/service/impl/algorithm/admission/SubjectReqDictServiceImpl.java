package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.service.algorithm.admission.SubjectReqDictService;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;
import com.haifeng.common.entity.algorithm.SubjectReqDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.SubjectReqDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectReqDictServiceImpl implements SubjectReqDictService {

    private final SubjectReqDictMapper subjectReqDictMapper;

    @Override
    public IPage<SubjectReqDictListVO> page(SubjectReqDictQueryDTO dto) {
        Page<SubjectReqDict> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectReqDict> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getCode())) {
            wrapper.like(SubjectReqDict::getCode, dto.getCode());
        }
        if (StringUtils.hasText(dto.getDisplayName())) {
            wrapper.like(SubjectReqDict::getDisplayName, dto.getDisplayName());
        }
        if (StringUtils.hasText(dto.getRequirementType())) {
            wrapper.eq(SubjectReqDict::getRequirementType, dto.getRequirementType());
        }

        wrapper.orderByAsc(SubjectReqDict::getSortOrder)
               .orderByAsc(SubjectReqDict::getId);

        IPage<SubjectReqDict> result = subjectReqDictMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public SubjectReqDictDetailVO detail(Integer id) {
        SubjectReqDict entity = subjectReqDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选科要求不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Integer add(SubjectReqDictAddDTO dto) {
        // 检查code唯一性
        if (existsByCode(dto.getCode(), null)) {
            throw new BusinessException(400, "标准代码已存在");
        }

        SubjectReqDict entity = new SubjectReqDict();
        BeanUtils.copyProperties(dto, entity);

        subjectReqDictMapper.insert(entity);
        log.info("新增选科要求成功，id={}, code={}", entity.getId(), entity.getCode());
        return entity.getId();
    }

    @Override
    public void update(Integer id, SubjectReqDictAddDTO dto) {
        SubjectReqDict existing = subjectReqDictMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "选科要求不存在");
        }

        // 检查code唯一性（排除自己）
        if (existsByCode(dto.getCode(), id)) {
            throw new BusinessException(400, "标准代码已存在");
        }

        BeanUtils.copyProperties(dto, existing);
        subjectReqDictMapper.updateById(existing);
        log.info("更新选科要求成功，id={}", id);
    }

    @Override
    public void delete(Integer id) {
        SubjectReqDict entity = subjectReqDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选科要求不存在");
        }

        subjectReqDictMapper.deleteById(id);
        log.info("删除选科要求成功，id={}", id);
    }

    @Override
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        subjectReqDictMapper.deleteBatchIds(ids);
        log.info("批量删除选科要求成功，ids={}", ids);
    }

    private boolean existsByCode(String code, Integer excludeId) {
        LambdaQueryWrapper<SubjectReqDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectReqDict::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SubjectReqDict::getId, excludeId);
        }
        return subjectReqDictMapper.selectCount(wrapper) > 0;
    }

    private SubjectReqDictListVO convertToListVO(SubjectReqDict entity) {
        SubjectReqDictListVO vo = new SubjectReqDictListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private SubjectReqDictDetailVO convertToDetailVO(SubjectReqDict entity) {
        SubjectReqDictDetailVO vo = new SubjectReqDictDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
