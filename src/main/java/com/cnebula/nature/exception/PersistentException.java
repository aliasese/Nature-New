package com.cnebula.nature.exception;

import com.cnebula.nature.constant.ResultStatusCode;
import com.cnebula.nature.vo.ResponseFormat;

/**
 * Created by calis on 2019/6/13.
 */
public class PersistentException extends Exception {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return new ResponseFormat(Boolean.FALSE, ResultStatusCode.PersistentException.getCode(), ResultStatusCode.PersistentException.getErrorMessageCN(), ResultStatusCode.PersistentException.getErrorMessageEn(), null).builder().toString();
    }

}
