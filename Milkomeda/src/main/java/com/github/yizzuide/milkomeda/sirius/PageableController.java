/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.github.yizzuide.milkomeda.comet.core.CometParam;
import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * BaseController
 *
 * @since 3.21.0
 * @author yizzuide
 * Create at 2025/05/29 04:04
 */
public class PageableController<S extends IPageableService<E>, E> {

    @Autowired
    protected S baseService;

    @GetMapping("list")
    public ResultVO<?> queryPage(@CometParam UniformQueryPageData<E> queryPageData) {
        UniformPage<E> uniformPage = baseService.selectByPage(queryPageData);
        return UniformResult.ok(uniformPage);
    }

    @GetMapping("all")
    public ResultVO<List<E>> queryAll() {
        return UniformResult.ok(baseService.list());
    }

    @PostMapping("add")
    public ResultVO<?> save(@CometParam E entity) {
        baseService.save(entity);
        return UniformResult.ok(null);
    }

    @PutMapping("update")
    public ResultVO<?> update(@CometParam E entity) {
        baseService.updateById(entity);
        return UniformResult.ok(null);
    }

    @DeleteMapping("del")
    public ResultVO<?> remove(E entity) {
        boolean isRemoved = baseService.removeBeforeCheckRef(entity);
        if (!isRemoved) {
            return UniformResult.error("2003", "当前记录被引用，不能删除！");
        }
        return UniformResult.ok(null);
    }

}
