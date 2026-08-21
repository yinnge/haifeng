package com.haifeng.admin.dto.fileload;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileLoadQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "文件名长度不能超过50")
    private String fileName;

    /** 学科筛选（数学/语文/英语等） */
    private String subject;

    /** 适合人群筛选（初一/初二/高一/高二等） */
    private String applicableStage;
}
