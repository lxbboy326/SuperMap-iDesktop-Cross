package com.supermap.desktop.CtrlAction;

import com.supermap.data.DatasetVector;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormTabular;
import com.supermap.desktop.Interface.IProperty;
import com.supermap.desktop.Interface.IPropertyManager;
import com.supermap.desktop.controls.property.WorkspaceTreeDataPropertyFactory;
import com.supermap.desktop.controls.property.dataset.RecordsetPropertyControl;
import com.supermap.desktop.implement.CtrlAction;

import java.util.ArrayList;

/**
 * @author XiaJT
 */
public class CtrlActionTabularProperty extends CtrlAction {
	public CtrlActionTabularProperty(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		DatasetVector dataset = ((IFormTabular) Application.getActiveApplication().getActiveForm()).getRecordset().getDataset();

		ArrayList<IProperty> properties = WorkspaceTreeDataPropertyFactory.getDatasetProperties(dataset);
		IPropertyManager propertyManager = Application.getActiveApplication().getMainFrame().getPropertyManager();
		propertyManager.setProperty(properties.toArray(new IProperty[properties.size()]));
		for (IProperty property : properties) {
			if (property instanceof RecordsetPropertyControl) {
				propertyManager.setSelectedProperty(property);
				break;
			}
		}
		propertyManager.setPropertyVisible(true);
	}

	@Override
	public boolean enable() {
		IForm activeForm = Application.getActiveApplication().getActiveForm();
		if (!(activeForm instanceof IFormTabular)) {
			return false;
		}
		if (((IFormTabular) activeForm).getRecordset() == null || ((IFormTabular) activeForm).getRecordset().getDataset() == null) {
			return false;
		}
		return true;
	}
}
