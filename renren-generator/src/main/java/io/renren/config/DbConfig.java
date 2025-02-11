/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.config;

import io.renren.mapper.*;
import io.renren.utils.RRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 数据库配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration
public class DbConfig {
    @Value("${renren.database: mysql}")
    private String database;
    @Autowired
    private MySQLGeneratorMapper mySQLGeneratorMapper;
    @Autowired
    private OracleGeneratorMapper oracleGeneratorMapper;
    @Autowired
    private SQLServerGeneratorMapper sqlServerGeneratorMapper;
    @Autowired
    private PostgreSQLGeneratorMapper postgreSQLGeneratorMapper;

    private static boolean mongo = false;

    @Bean
    @Primary
    @Conditional(MongoNullCondition.class)
    public GeneratorMapper getGeneratorMapper() {
        if ("mysql".equalsIgnoreCase(database)) {
            return mySQLGeneratorMapper;
        } else if ("oracle".equalsIgnoreCase(database)) {
            return oracleGeneratorMapper;
        } else if ("sqlserver".equalsIgnoreCase(database)) {
            return sqlServerGeneratorMapper;
        } else if ("postgresql".equalsIgnoreCase(database)) {
            return postgreSQLGeneratorMapper;
        } else {
            throw new RRException("不支持当前数据库：" + database);
        }
    }

    @Bean
    @Primary
    @Conditional(MongoCondition.class)
    public GeneratorMapper getMongoDBMapper(MongoDBGeneratorMapper mongoDBGeneratorMapper) {
        mongo = true;
        return mongoDBGeneratorMapper;
    }

    public static boolean isMongo() {
        return mongo;
    }

}
