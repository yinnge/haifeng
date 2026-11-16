package com.haifeng.app.config;

import com.haifeng.common.config.PlannerServiceItemListTypeHandler;
import com.haifeng.common.dto.home.PlannerServiceItem;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 services JSONB 反序列化为强类型 List&lt;PlannerServiceItem&gt;，
 * 而非退化的 List&lt;LinkedHashMap&gt;（后者写进 Redis 会触发 type id 冲突）。
 */
class PlannerServiceItemListTypeHandlerTest {

    private final PlannerServiceItemListTypeHandler handler = new PlannerServiceItemListTypeHandler();

    @Test
    void readsStronglyTypedItems() throws Exception {
        String json = "[{\"title\":\"首次诊断\",\"content\":\"成绩位次评估\"}," +
                "{\"title\":\"方案初稿\",\"content\":\"冲稳保梯度设计\"}]";
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("services")).thenReturn(json);

        List<PlannerServiceItem> items = handler.getNullableResult(rs, "services");

        assertThat(items).hasSize(2);
        assertThat(items.get(0)).isInstanceOf(PlannerServiceItem.class);
        assertThat(items.get(0).getTitle()).isEqualTo("首次诊断");
        assertThat(items.get(0).getContent()).isEqualTo("成绩位次评估");
        assertThat(items.get(1).getTitle()).isEqualTo("方案初稿");
    }

    @Test
    void blankJson_returnsEmptyList() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("services")).thenReturn(null);

        assertThat(handler.getNullableResult(rs, "services")).isEmpty();
    }
}
