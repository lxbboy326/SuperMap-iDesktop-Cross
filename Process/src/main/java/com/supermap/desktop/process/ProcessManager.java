package com.supermap.desktop.process;

import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.loader.DefaultProcessGroup;
import com.supermap.desktop.process.loader.IProcessDescriptor;
import com.supermap.desktop.process.loader.IProcessGroup;
import com.supermap.desktop.process.loader.IProcessLoader;
import com.supermap.desktop.utilities.StringUtilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by highsad on 2017/8/4.
 */
public class ProcessManager extends DefaultProcessGroup {
	private Map<String, IProcessGroup> groupMap = new ConcurrentHashMap<>();
	private Map<String, IProcessLoader> loaderMap = new ConcurrentHashMap<>();

	public final static ProcessManager INSTANCE = new ProcessManager();

	public IProcess createProcess(IProcessDescriptor processDescriptor) {
		if (processDescriptor == null) {
			return null;
		}

		String className = processDescriptor.getClassName();
		if (StringUtilities.isNullOrEmpty(className) || !this.loaderMap.containsKey(className)) {
			return null;
		}

		return this.loaderMap.get(className).loadProcess(processDescriptor);
	}

	public void registerProcessLoader(String className, IProcessLoader loader) {
		if (this.loaderMap.containsKey(className)) {
			return;
		}

		this.loaderMap.put(className, loader);
	}
}
