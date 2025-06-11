package com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.command;

import lombok.Data;

/**
 * 下单命令
 *
 * @author yizzuide
 * Create at 2025/06/09 17:01
 */
@Data
public class PlaceCommand {
    private Long userId;
    private String from;
    private String to;
}
