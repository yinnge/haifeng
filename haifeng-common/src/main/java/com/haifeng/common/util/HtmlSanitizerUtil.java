package com.haifeng.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 富文本 HTML 白名单清洗工具（防存储型 XSS）
 * <p>用于用户/管理员输入的富文本正文入库前清洗：只保留白名单标签与属性，
 * script/iframe/事件属性（onclick 等）一律剔除。
 */
public final class HtmlSanitizerUtil {

    /**
     * 富文本白名单：
     * - relaxed() 基线已包含常用标签（p/h1~h6/ul/ol/li/img/a/strong/em/blockquote/pre/code
     *   及全部表格标签 table/thead/tbody/tr/th/td 等），无需再 addTags；
     * - 追加所有标签可携带 style/class（前端排版需要）；
     * - img src 在 relaxed 默认 http/https 基础上追加 data:（富文本编辑器粘贴截图为 base64 data:image）。
     */
    private static final Safelist RICH_TEXT_SAFELIST = Safelist.relaxed()
            .addAttributes(":all", "style", "class")
            .addProtocols("img", "src", "data");

    /**
     * prettyPrint=false：清洗时不插入换行/缩进，避免破坏 pre 与行内标签排版。
     * 注意：必须通过 Jsoup.clean(html, baseUri, safelist, outputSettings) 传入才生效。
     */
    private static final Document.OutputSettings OUTPUT_SETTINGS =
            new Document.OutputSettings().prettyPrint(false);

    private HtmlSanitizerUtil() {
    }

    /**
     * 清洗富文本 HTML，返回仅含白名单内容的安全 HTML。
     * 空串/null 原样返回（交由上层 @NotBlank 校验）。
     */
    public static String sanitizeRichText(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return Jsoup.clean(html, "", RICH_TEXT_SAFELIST, OUTPUT_SETTINGS);
    }
}
