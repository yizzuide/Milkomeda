package com.github.yizzuide.milkomeda.demo.neutron;

import com.github.yizzuide.milkomeda.neutron.Neutron;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NeutronController
 *
 * @author yizzuide
 * Create at 2019/12/10 00:25
 */
@RestController
@RequestMapping("neutron")
public class NeutronController {

    private static String jobName = "neutron_rotation";

    @RequestMapping("add")
    public ResponseEntity<?> add() {
        Neutron.addJob(jobName, NeutronJob.class, "1/5 * * * * ?");
        // 动态添加实现
        // String clazzName = 从数据库读取配置的class
        // Class clazz = Class.forName(clazzName)
        // Neutron.addJob(jobName, (Class<? extends Job>)clazz, "1/5 * * * * ?")
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping("remove")
    public ResponseEntity<?> remove() {
        Neutron.removeJob(jobName);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping("update")
    public ResponseEntity<?> update() {
        Neutron.modifyJobTime(jobName, "* * * * * ?");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
