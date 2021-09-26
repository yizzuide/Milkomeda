/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MultiDataSourceTransaction
 * 多数据源事务
 *
 * @author yizzuide
 * @since 3.7.1
 * Create at 2020/05/31 11:48
 */
@Slf4j
public class MultiDataSourceTransaction implements Transaction {

    private final DataSource dataSource;

    private Connection mainConnection;

    private final String mainDataSourceType;

    private final ConcurrentMap<String, Connection> otherConnectionMap;

    private boolean isConnectionTransactional;

    private boolean autoCommit;

    public MultiDataSourceTransaction(DataSource dataSource) {
        Assert.notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
        mainDataSourceType = SundialHolder.getDataSourceType();
        otherConnectionMap = new ConcurrentHashMap<>();
    }

    @Override
    public Connection getConnection() throws SQLException {
        // 动态的根据DatabaseType获取不同的Connection
        String dataSourceType = SundialHolder.getDataSourceType();
        if (dataSourceType.equals(mainDataSourceType)) {
            if (mainConnection == null) {
                openMainConnection();
            }
            return mainConnection;
        } else {
            if (!otherConnectionMap.containsKey(dataSourceType)) {
                try {
                    Connection conn = dataSource.getConnection();
                    otherConnectionMap.put(dataSourceType, conn);
                    return conn;
                } catch (SQLException ex) {
                    throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
                }
            }
            return otherConnectionMap.get(dataSourceType);
        }
    }

    private void openMainConnection() throws SQLException {
        this.mainConnection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommit = this.mainConnection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.mainConnection, this.dataSource);
        if (log.isDebugEnabled()) {
            log.debug("Sundial JDBC Connection [" + this.mainConnection + "] will" +
                    (this.isConnectionTransactional ? " " : " not ") + "be managed by Spring");
        }
    }

    @Override
    public void commit() throws SQLException {
        // 非Spring事务管理的提交处理
        if (this.mainConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (log.isDebugEnabled()) {
                log.debug("Sundial Committing JDBC Connection [" + this.mainConnection + "]");
            }
            this.mainConnection.commit();
            for (Connection connection : otherConnectionMap.values()) {
                connection.commit();
            }
        }
    }

    @Override
    public void rollback() throws SQLException {
        // 非Spring事务管理的回滚处理
        if (this.mainConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (log.isDebugEnabled()) {
                log.debug("Sundial Rolling back JDBC Connection [" + this.mainConnection + "]");
            }
            this.mainConnection.rollback();
            for (Connection connection : otherConnectionMap.values()) {
                connection.rollback();
            }
        }
    }

    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.mainConnection, this.dataSource);
        for (Connection connection : otherConnectionMap.values()) {
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }

    @Override
    public Integer getTimeout() throws SQLException {
        // 根据当前配置的事务超时来设置超时
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
        return holder != null && holder.hasTimeout() ? holder.getTimeToLiveInSeconds() : null;
    }
}
