package com.haifeng.app.dto.competition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端竞赛列表查询 DTO */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionListQueryDTO extends BasePageQueryDTO {

    /** 竞赛名称模糊查询 */
    @Size(max = 100, message = "竞赛名称长度不能超过100")
    private String compName;

    /** 竞赛级别精准查询（国家级/省级/校级等） */
    @Size(max = 20, message = "竞赛级别长度不能超过20")
    private String compLevel;
}
