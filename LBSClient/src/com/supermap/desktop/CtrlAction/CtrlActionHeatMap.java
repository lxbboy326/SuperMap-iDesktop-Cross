package com.supermap.desktop.CtrlAction;


import javax.swing.JFrame;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.lbsclient.LBSClientProperties;
import com.supermap.desktop.ui.UICommonToolkit;

public class CtrlActionHeatMap extends CtrlAction {

	public CtrlActionHeatMap(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		try {
			JFrame parent = (JFrame)Application.getActiveApplication().getMainFrame();
			JDialogHeatMap dialog = new JDialogHeatMap(parent, true);
			dialog.setVisible(true);
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		return true;
	}
}
