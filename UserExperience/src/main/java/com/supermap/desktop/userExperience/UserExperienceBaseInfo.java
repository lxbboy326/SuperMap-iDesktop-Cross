package com.supermap.desktop.userExperience;

import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.supermap.desktop.interfaces.UserExperienceBean;

import java.lang.management.ManagementFactory;

/**
 * @author XiaJT
 */
public class UserExperienceBaseInfo implements UserExperienceBean, JSONAware {

	private String IP = "{IP}";
	private String MACADDRESS = "{MACADDRESS}";
	private String productID = "iDesktopCross";

	private DesktopUserExperienceInfo desktopUserExperienceInfo;

	public UserExperienceBaseInfo(DesktopUserExperienceInfo desktopUserExperienceInfo) {
		this.desktopUserExperienceInfo = desktopUserExperienceInfo;
	}

	@Override
	public final String getJson() {
		// TODO: 2017/7/11
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("IP", IP);
		jsonObject.put("MACAddress", MACADDRESS);
		jsonObject.put("ProductID", productID);
		jsonObject.put("ProductInfo", desktopUserExperienceInfo);
		jsonObject.put("ThreadID", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		return jsonObject.toJSONString();
	}

	@Override
	public String toJSONString() {
		return getJson();
	}
}
