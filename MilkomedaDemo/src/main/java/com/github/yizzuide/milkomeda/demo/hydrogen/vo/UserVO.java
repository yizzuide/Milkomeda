package com.github.yizzuide.milkomeda.demo.hydrogen.vo;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * UserVO
 *
 * @author yizzuide
 * <br>
 * Create at 2020/03/27 11:19
 */
@Data
public class UserVO {
    @NotEmpty
    @Size(min = 6)
    private String username;
}
