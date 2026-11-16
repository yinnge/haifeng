package com.haifeng.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.entity.home.InstitutionClassType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * t_institutions.class_types（jsonb 数组）与 List&lt;InstitutionClassType&gt; 互转。
 * <p>不能直接用 {@link JsonbTypeHandler}/{@code JacksonTypeHandler}：属性类型是 List，
 * Jackson 反序列化会退化成 List&lt;LinkedHashMap&gt;，这里显式指定元素的 JavaType。</p>
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class InstitutionClassTypeListTypeHandler extends BaseTypeHandler<List<InstitutionClassType>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final JavaType LIST_TYPE = OBJECT_MAPPER.getTypeFactory()
            .constructCollectionType(List.class, InstitutionClassType.class);

    public InstitutionClassTypeListTypeHandler() {
    }

    public InstitutionClassTypeListTypeHandler(Class<?> type) {
        // MyBatis-Plus 通过 @TableField(typeHandler = ...) 注册时会传属性类型，这里固定按 class_types 解析
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<InstitutionClassType> parameter,
                                    JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            jsonObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("序列化主打班型失败", e);
        }
        ps.setObject(i, jsonObject);
    }

    @Override
    public List<InstitutionClassType> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<InstitutionClassType> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<InstitutionClassType> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<InstitutionClassType> parse(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new SQLException("解析主打班型失败", e);
        }
    }
}
