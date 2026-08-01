package com.haifeng.app.vo.home;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteInfoVO {

    private static final long serialVersionUID = 1L;

    private String siteIcp;

    private String siteDescription;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;

    /** Pro 会员年价格（分/元） */
    private Integer proPrice;

    /** VIP 会员年价格（分/元） */
    private Integer vipPrice;
}
