package com.github.yizzuide.milkomeda.demo.fusion.handler;

import com.github.yizzuide.milkomeda.demo.fusion.vo.ReturnVO;
import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.fusion.FusionAction;
import com.github.yizzuide.milkomeda.fusion.FusionHandler;
import com.github.yizzuide.milkomeda.fusion.FusionMetaData;

/**
 * APIResponseDataHandler
 *
 * @author yizzuide
 * Create at 2020/05/05 16:41
 */
@FusionHandler
public class APIResponseDataHandler {

    @FusionAction("api")
    public ReturnVO<?> apiAction(FusionMetaData<Product> metaData) {
        // 返回错误类型响应数据
        if (metaData.isError()) {
            return new ReturnVO<>().error(metaData.getMsg());
        }
        return new ReturnVO<>().ok(metaData.getReturnData());
    }

    @FusionAction("product-push")
    public Object productAction(FusionMetaData<Long> metaData) {
        return 0;
    }
}
