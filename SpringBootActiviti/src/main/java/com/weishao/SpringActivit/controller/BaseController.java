package com.weishao.SpringActivit.controller;

import java.util.HashMap;
import java.util.Map;

public class BaseController {
	
    protected Map<String,Object> success(Map<String,Object> data) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("errcode",0);
        map.put("errmsg","success");
        map.put("data", data);
        return map;
    }

    protected Map<String, Object> failed(long errno,String reason) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("errcode",errno);
        map.put("errmsg",reason);
        return map;
    }
}
