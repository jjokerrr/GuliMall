package com.mall.ware.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
public class MybatisPlusConfigration extends BaseTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, String s, JdbcType jdbcType) throws SQLException {
        preparedStatement.setBytes(i, s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, String s) throws SQLException {
        byte[] bytes = resultSet.getBytes(s);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public String getNullableResult(ResultSet resultSet, int i) throws SQLException {
        byte[] bytes = resultSet.getBytes(i);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public String getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        byte[] bytes = callableStatement.getBytes(i);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
