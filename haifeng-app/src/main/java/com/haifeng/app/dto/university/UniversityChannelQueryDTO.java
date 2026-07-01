package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityChannelQueryDTO extends BasePageQueryDTO {
    private String channelName;
    private String regionTag;
}
