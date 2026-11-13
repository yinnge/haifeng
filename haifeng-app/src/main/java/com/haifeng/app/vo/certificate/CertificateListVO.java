package com.haifeng.app.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端证书列表 VO（spec 任务1接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 雪花 ID：序列化为字符串，避免前端 JS Number 精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private String registrationTime;
    private String examTime;
    private Integer examFee;
    private String certIntro;
}
