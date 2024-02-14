package com.github.yizzuide.milkomeda.demo.hydrogen.service;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.annotation.Resource;
import java.util.Date;

/**
 * TOrderService
 *
 * @author yizzuide
 * <br>
 * Create at 2020/03/25 21:51
 */
@Service("tOrderService")
public class TOrderService {
    @Resource
    private TOrderMapper tOrderMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void testTx() {
        TOrder tOrder = new TOrder();
        tOrder.setUserId(101L);
        tOrder.setOrderNo(1435433467657L);
        tOrder.setProductId(151L);
        tOrder.setProductName("iPhone");
        tOrder.setPrice(8900L);
        Date now = new Date();
        tOrder.setCreateTime(now);
        tOrder.setUpdateTime(now);
        tOrderMapper.insert(tOrder);

        // 模拟抛出RuntimeException
        // int i = 1/0;

        // 模拟事务超时
        /*try {
            Thread.sleep(5200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        TOrder tOrder2 = new TOrder();
        tOrder2.setUserId(102L);
        tOrder2.setOrderNo(14354567899000L);
        tOrder2.setProductId(156L);
        tOrder2.setProductName("Mac");
        tOrder2.setPrice(28900L);
        tOrder2.setCreateTime(now);
        tOrder2.setUpdateTime(now);
        tOrderMapper.insert(tOrder2);
    }

    public void testTransactionByManual() {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // do something...
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
        }
    }
}
