package com.haifeng.common.handler;

import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.SpringContextHolder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(String.class)
public class AESEncryptTypeHandler extends BaseTypeHandler<String> {

    private String getKey() {
        SecurityProperties properties = SpringContextHolder.getBean(SecurityProperties.class);
        return properties.getAesKey();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, CryptoUtil.encrypt(parameter, getKey()));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return CryptoUtil.decrypt(value, getKey());
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return CryptoUtil.decrypt(value, getKey());
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return CryptoUtil.decrypt(value, getKey());
    }
}
