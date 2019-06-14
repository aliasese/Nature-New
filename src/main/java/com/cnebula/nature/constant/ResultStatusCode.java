package com.cnebula.nature.constant;

/**
 * Created by calis on 2019/6/13.
 */
public enum ResultStatusCode {

    SystemError(10001, "系统错误","System error"),
    ServiceUnavailable(10002, "服务不可用","Service unavailable"),
    PersistentException(10003, "持久化文章信息异常,可能缺少必要字段", "Persistent article Exception for some field may can not be blank");

    private Integer code;
    private String errorMessageCN;
    private String errorMessageEn;

    ResultStatusCode(Integer code, String errorMessageCN, String errorMessageEn) {
        this.code = code;
        this.errorMessageCN = errorMessageCN;
        this.errorMessageEn = errorMessageEn;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrorMessageCN() {
        return errorMessageCN;
    }

    public void setErrorMessageCN(String errorMessageCN) {
        this.errorMessageCN = errorMessageCN;
    }

    public String getErrorMessageEn() {
        return errorMessageEn;
    }

    public void setErrorMessageEn(String errorMessageEn) {
        this.errorMessageEn = errorMessageEn;
    }
}
