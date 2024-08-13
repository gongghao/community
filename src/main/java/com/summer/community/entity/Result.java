package com.summer.community.entity;

import com.alibaba.fastjson.JSONObject;
import com.summer.community.util.CommunityConstant;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2023-12-06-1:12
 */
public class Result implements CommunityConstant {
    private Boolean success;
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer setCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    //把构造方法私有
    public Result(){}

    public Result(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("RESULT_SUCCESS", success);
        json.put("RESULT_CODE", code);
        json.put("RESULT_MESSAGE", message);
        if (data != null)
            for (String key : data.keySet())
                json.put(key, data.get(key));
        return json.toJSONString();
    }

    //成功静态方法
    public static Result ok(String message){
        Result r= new Result();
        r.setSuccess(true);
        r.setCode(CommunityConstant.SUCCESS);
        r.setMessage(message);
        return r;
    }

    //失败静态方法
    public static Result error(String message){
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(CommunityConstant.ERROR);
        r.setMessage(message);
        return r;
    }


    public Result success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public Result message(String message){
        this.setMessage(message);
        return this;
    }

    public Result code(Integer code){
        this.setCode(code);
        return this;
    }

    public Result data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public Result data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}
