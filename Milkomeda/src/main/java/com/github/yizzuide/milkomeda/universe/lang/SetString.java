/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.lang;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * String type of set.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/27 15:12
 */
@Data
public class SetString {

    private String source;

    public SetString(String source) {
        this.source = source;
    }

    public boolean add(String item) {
        if (this.source.contains(item)) {
            return false;
        }
        if (StringUtils.isEmpty(this.source)) {
            this.source = item;
            return true;
        }
        this.source = this.source.concat(",").concat(item);
        return true;
    }

    public Set<String> toSet() {
        String[] items = this.source.split(",");
        Set<String> set = new HashSet<>();
        Collections.addAll(set, items);
        return set;
    }

    public String toString() {
        return this.source;
    }
}
