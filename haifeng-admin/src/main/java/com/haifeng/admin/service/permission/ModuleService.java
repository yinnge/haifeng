package com.haifeng.admin.service.permission;

import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.vo.permission.ModuleTreeVO;

import java.util.List;

public interface ModuleService {

    List<ModuleTreeVO> listTree(ModuleQueryDTO dto);

    void add(ModuleAddDTO dto);

    void update(Long id, ModuleUpdateDTO dto);

    void delete(Long id);

    void toggleStatus(Long id);
}
