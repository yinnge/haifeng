
## **1.2 系统设置表 (system_settings)**（设置网站的logos等）


| 字段名             | 类型           | 约束                                    | 说明             |
|-----------------| ------------ | ------------------------------------- | -------------- |
| id              | SERIAL       | PRIMARY KEY                           | 设置ID           |
| site_name       | VARCHAR(50)  |                                       | 网站名称           |
| site_url        | VARCHAR(100) |                                       | 网站Logo         |
| site_icp        | VARCHAR(100) |                                       | ICP备案号         |
| site_descption  | Text         |                                       | 网站描述           |
| api_number      | integer      | DEFAULT 3                             | api调用限制        |
| pro_price       | INT          | DEFAULT 199                           | 会员价格           |
| vip_price       | INT          | DEFAULT 599                           | vip会员价格        |
| seo_title       | VARCHAR(200) |                                       | SEO标题（只展示首页即可） |
| seo_keywords    | VARCHAR(100) |                                       | SEO关键词         |
| seo_description | TEXT         |                                       | SEO描述          |
| updated_at      | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP             | 更新时间           |
| wechat_ew_url   | VARCHAR(100) |                                       | 微信url          |
| contact_url     | JSONB        | wechat,weibo,zhihu,douyin,b站          | url            |
| basic_message   | JSONB        | address,phone,email,consultation_time | 全是字符串          |
