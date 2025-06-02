/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.util

/**
 * Collection util
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 19:10
 */
class Collections {
    // kotlin没有static, 通过拌生对象 companion object 创建，默认名为Companion
    companion object Creator {

        @JvmField
        val DEFAULT_ARRAYLIST_LOAD_FACTOR = 1.5f

        @JvmField
        val DEFAULT_HASHMAP_LOAD_FACTOR = 0.75f

        /**
         * 根据底层扩容机制，创建一个合理大小的 ArrayList
         */
        @JvmStatic
        fun <T> list(size: Int): ArrayList<T> {
            val initialCapacity = if (size <= 0) 10 else getCapacity((size / DEFAULT_ARRAYLIST_LOAD_FACTOR).toInt())
            return ArrayList(initialCapacity)
        }

        /**
         * 根据底层扩容机制，创建一个合理大小的 HashMap
         */
        @JvmStatic
        fun <K, V> map(size: Int): HashMap<K, V> {
            val initialCapacity = if (size <= 0) 16 else getCapacity((size / DEFAULT_HASHMAP_LOAD_FACTOR).toInt())
            return HashMap(initialCapacity, DEFAULT_HASHMAP_LOAD_FACTOR)
        }

        /**
         * 计算合理的初始容量（确保是 2 的幂）
         */
        private fun getCapacity(initialCapacity: Int): Int {
            var capacity = 1
            while (capacity < initialCapacity) {
                // capacity左移一位，相当于 capacity *= 2
                capacity = capacity shl 1
            }
            return capacity
        }
    }
}

fun singletonMap(key: String, value: Any): Map<String, Any> = mapOf(key to value)