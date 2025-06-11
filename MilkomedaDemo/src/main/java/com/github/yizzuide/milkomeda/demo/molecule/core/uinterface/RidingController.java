package com.github.yizzuide.milkomeda.demo.molecule.core.uinterface;

import com.github.yizzuide.milkomeda.demo.molecule.core.application.service.RidingAppService;
import com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.command.PlaceCommand;
import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RidingController
 *
 * @author yizzuide
 * Create at 2025/06/09 16:58
 */
@RestController
@RequestMapping("riding")
public class RidingController {

    @Autowired
    private RidingAppService ridingAppService;

    @PostMapping("place")
    public ResultVO<String> place(PlaceCommand placeCommand) {
        // 从安全上下文获取
        placeCommand.setUserId(1L);
        return UniformResult.ok(ridingAppService.place(placeCommand));
    }

}
