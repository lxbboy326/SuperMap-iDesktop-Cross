package com.supermap.desktop.process.parameter.ipls;

import com.alibaba.fastjson.JSON;
import com.supermap.data.DatasetType;
import com.supermap.desktop.lbs.IServerServiceImpl;
import com.supermap.desktop.lbs.Interface.IServerService;
import com.supermap.desktop.lbs.params.IServerLoginInfo;
import com.supermap.desktop.lbs.params.QueryDatasetNamesResult;
import com.supermap.desktop.lbs.params.QueryDatasetTypeResult;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.StringUtilities;
import org.apache.http.impl.client.CloseableHttpClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author XiaJT
 */
public class ParameterIServerLogin extends ParameterCombine {
	private ParameterDefaultValueTextField parameterTextFieldAddress = new ParameterDefaultValueTextField(CoreProperties.getString("String_Server"));
	private ParameterDefaultValueTextField parameterTextFieldUserName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_UserName"));
	private ParameterPassword parameterTextFieldPassword = new ParameterPassword(ProcessProperties.getString("String_PassWord"));
	public IServerServiceImpl service;

	public ParameterIServerLogin() {
		super();
		parameterTextFieldAddress.setRequisite(true);
		parameterTextFieldAddress.setDefaultWarningValue("{ip}:{port}");
		parameterTextFieldUserName.setRequisite(true);
		parameterTextFieldPassword.setRequisite(true);
		this.addParameters(parameterTextFieldAddress, parameterTextFieldUserName, parameterTextFieldPassword);
		this.setDescribe(ProcessProperties.getString("String_loginInfo"));
		registerEvents();
	}

	private void registerEvents() {

	}

	public synchronized boolean login() {
		boolean result = false;
		String username = parameterTextFieldUserName.getSelectedItem();
		String password = (String) parameterTextFieldPassword.getSelectedItem();
		String serviceInfo = parameterTextFieldAddress.getSelectedItem();
		if (StringUtilities.isNullOrEmpty(serviceInfo) || StringUtilities.isNullOrEmpty(username)
				|| StringUtilities.isNullOrEmpty(password)) {
			return result;
		}
		service = new IServerServiceImpl();
		if (serviceInfo.contains(":") && serviceInfo.split(":").length > 1) {
			IServerLoginInfo.ipAddr = serviceInfo.split(":")[0];
			IServerLoginInfo.port = serviceInfo.split(":")[1];
		}
		IServerLoginInfo.username = username;
		IServerLoginInfo.password = password;
		CloseableHttpClient client = service.login(username, password);
		if (null != client) {
			result = true;
			IServerLoginInfo.client = client;
		}
		return result;
	}

	public IServerService getService() {
		return service;
	}
}