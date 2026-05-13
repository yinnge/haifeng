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

    private String siteIcp;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;
}
