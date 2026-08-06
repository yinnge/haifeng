package com.haifeng.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;

import java.util.List;

/**
 * 专门处理 List<HistoryScore> 的 JSONB 反序列化。
 * 继承 JsonbTypeHandler 以复用 setNonNullParameter 的 jsonb 写入逻辑，
 * 重写 parse 以使用 TypeReference 保留泛型类型信息。
 */
public class HistoryScoreListTypeHandler extends JsonbTypeHandler {

    private static final TypeReference<List<WishMajorSnapshot.HistoryScore>> TYPE_REF =
            new TypeReference<List<WishMajorSnapshot.HistoryScore>>() {};

    public HistoryScoreListTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public Object parse(String json) {
        try {
            return getObjectMapper().readValue(json, TYPE_REF);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
