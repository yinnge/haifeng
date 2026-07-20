package com.haifeng.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class BigDecimalListTypeHandler extends BaseTypeHandler<List<BigDecimal>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<BigDecimal> parameter, JdbcType jdbcType) throws SQLException {
        Connection conn = ps.getConnection();
        BigDecimal[] array = parameter.toArray(new BigDecimal[0]);
        java.sql.Array sqlArray = conn.createArrayOf("numeric", array);
        ps.setArray(i, sqlArray);
    }

    @Override
    public List<BigDecimal> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toList(rs.getArray(columnName));
    }

    @Override
    public List<BigDecimal> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toList(rs.getArray(columnIndex));
    }

    @Override
    public List<BigDecimal> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toList(cs.getArray(columnIndex));
    }

    private List<BigDecimal> toList(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) return null;
        Object[] array = (Object[]) sqlArray.getArray();
        List<BigDecimal> result = new ArrayList<>(array.length);
        for (Object obj : array) {
            result.add(obj instanceof BigDecimal ? (BigDecimal) obj : new BigDecimal(obj.toString()));
        }
        return result;
    }
}
