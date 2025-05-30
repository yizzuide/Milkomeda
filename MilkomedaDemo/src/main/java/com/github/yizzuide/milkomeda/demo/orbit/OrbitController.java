package com.github.yizzuide.milkomeda.demo.orbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OrbitController
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/21 02:09
 */
@RequestMapping("orbit")
@RestController
public class OrbitController {

    @Autowired
    private OrderAPI orderAPI;

    @RequestMapping("find/{orderNo}")
    public ResponseEntity<String> findOrder(@PathVariable String orderNo) {
        orderAPI.fetchOrder(orderNo);
        return ResponseEntity.ok("OK");
    }

    @RequestMapping("push/{orderNo}")
    public ResponseEntity<String> pushOrder(@PathVariable String orderNo) {
        orderAPI.pushOrder(orderNo);
        return ResponseEntity.ok("OK");
    }

    @RecordLog
    @RequestMapping("pay/{orderNo}")
    public ResponseEntity<String> payOrder(@PathVariable String orderNo) {
        return ResponseEntity.ok("OK");
    }
}
