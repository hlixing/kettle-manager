/**
 * Copyright (c) 2013-2015, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.oss.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import net.oschina.kettleutil.common.KuConst;
import net.oschina.mytuils.DesUtil;
import net.oschina.mytuils.Dict;
import net.oschina.mytuils.StringUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.engine.EovaExp;
import com.eova.model.User;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
* 公用控制器<br/>
* date: 2016年5月29日 <br/>
* @author jingma
* @version 
*/
public class CommonController extends Controller {

	/**
	* 日志
	*/
	Log log = Log.getLog(getClass()); 
	/**
	* 下载文件 <br/>
	* @author jingma
	* @throws Exception
	*/
	public void download() throws Exception {
		String fileDir = getPara("fileDir");
		User user = (User)getSessionAttr("user");
		log.info(user.getStr("nickname")+"获取："+fileDir);
		renderFile(fileDir);
	}
    /**
    * 下载文件-绝对路径 <br/>
    * @author jingma
    * @throws Exception
    */
    public void downloadJdlj() throws Exception {
        String fileDir = getPara("fileDir");
        User user = (User)getSessionAttr("user");
        log.info(user.getStr("nickname")+"获取："+fileDir);
        File f = new File(fileDir);
        if(f.getAbsolutePath().matches(EovaConfig.props.get("download_jdlj_pattern"))){
            renderFile(f);
        }else{
            renderText("该文件不再允许下载范围内");
        }
    }
    /**
    * 验证值在表中是否不存在 <br/>
    * @author jingma
    */
    public void notExistValueByTable(){
        String table = getPara("table");
        String ds = getPara("ds");
        if(StringUtil.isBlank(ds)){
            ds = xx.DS_MAIN;
        }
        String name = getPara("name");
        String value = getPara(name);
        String oid = getPara(KuConst.FIELD_OID);
        String sql = "select 1 from "+table+" t where t."+name+"=?";
        JSONObject result = new JSONObject();
        if(StringUtil.isBlank(value)){
            result.put("ok", "");
            renderJson(result);
            return;
        }
        List<Object> param = new ArrayList<Object>();
        param.add(value);
        if(StringUtil.isNotBlank(oid)){
            sql += " and t.oid<>?";
            param.add(oid);
        }
        List<Record> recordList = Db.use(ds).find(sql,param.toArray());
        if(recordList.size()==0){
            result.put("ok", "");
        }else{
            result.put("error", "已经存在");
        }
        renderJson(result);
    }
	/**
	* 验证值是否不存在 <br/>
	* @author jingma
	*/
	public void notExistValue(){
	    String expStr = getPara("exp");
	    expStr = Dict.dictCategoryToSql(expStr);
        String value = getPara(getPara("name"));
        String oid = getPara(KuConst.FIELD_OID);
        List<Object> param = new ArrayList<Object>();
        param.add(value);
        param.add(oid);
        param.remove(null);
	    EovaExp exp = new EovaExp(expStr);
	    List<Record> recordList = Db.use(exp.ds).find(exp.sql,param.toArray());
	    JSONObject result = new JSONObject();
	    if(recordList.size()==0){
	        result.put("ok", "");
	    }else{
            result.put("error", "已经存在");
	    }
        renderJson(result);
	}
	/**
	* 将用户信息加密，然后重定向到指定url <br/>
	* @author jingma
	*/
	public void doDesEncryptUrl(){
	    String url = getPara("url");
	    String[] urlArr = url.split("@");
        User user = (User)getSessionAttr(EovaConst.USER);
        String userInfo = Dict.getDbCurrentDateLL()+"@"+user.getStr("id");
	    if(urlArr.length==2){
	        JSONObject expand = JSON.parseObject(Dict.dictObj("PROJECT", 
	                urlArr[0]).getStr(KuConst.FIELD_EXPAND));
	        url = expand.getString("host")+urlArr[1];
	        userInfo = DesUtil.encrypt(userInfo, expand.getString("pwd"))
	                .replace("\n", "").replace("\r", "");
	    }
        try {
            url += "&userInfo="+URLEncoder.encode(userInfo,"utf8");
        } catch (UnsupportedEncodingException e) {
            log.error("用户信息编码失败", e);
            renderText("用户信息编码失败");
            return;
        }
        redirect("http://"+url);
	}
}