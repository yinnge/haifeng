package com.haifeng.admin.vo.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpEnableVO {

    /**
     * TOTP 密钥（用于手动输入）
     */
    private String secret;

    /**
     * Base64 编码的二维码图片
     */
    private String qrCodeImage;
}
