package com.cnebula.nature.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Created by calis on 2019/6/13.
 */
@AllArgsConstructor
@Builder
public class ResponseFormat {

    private Boolean success;
    private Integer code;
    private String messageCN;
    private String messageEN;
    private Object data;

}
