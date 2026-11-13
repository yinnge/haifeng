package com.haifeng.app.vo.special;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelUnivListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 通道-大学关联记录主键，详情接口据此定位记录 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long universityId;
    private String universityName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
