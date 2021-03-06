package com.supermap.desktop.CtrlAction.transformationForm.CtrlAction;

import com.supermap.data.Datasource;
import com.supermap.data.Datasources;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.CtrlAction.transformationForm.Dialogs.NewTransformations.JDialogNewTransformation;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormTransformation;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.utilities.XmlUtilities;

/**
 * @author XiaJT
 */
public class CtrlActionNewTransformationForm extends CtrlAction {
	public CtrlActionNewTransformationForm(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		JDialogNewTransformation jDialogNewTransformationForm = new JDialogNewTransformation();
		if (jDialogNewTransformationForm.showDialog() == DialogResult.OK) {
			IFormTransformation iFormTransformation = (IFormTransformation) CommonToolkit.FormWrap.fireNewWindowEvent(WindowType.TRANSFORMATION, "");
			iFormTransformation.setSelectedColor(jDialogNewTransformationForm.getSelectedColor());
			iFormTransformation.setUnSelectedColor(jDialogNewTransformationForm.getUnSelectedColor());
			iFormTransformation.setUnUseColor(jDialogNewTransformationForm.getUnUseColor());
			iFormTransformation.addReferenceObjects(jDialogNewTransformationForm.getReferenceObjects());
			iFormTransformation.addTargetObjects(jDialogNewTransformationForm.getTargetObjects());
			if (jDialogNewTransformationForm.isSelectTransformationFile()) {
				iFormTransformation.fromXml(XmlUtilities.getDocument(jDialogNewTransformationForm.getSelectTransformationFilePath()));
			} else {
				iFormTransformation.setTransformationMode(jDialogNewTransformationForm.getTransformationMode());
			}
		}
		jDialogNewTransformationForm.dispose();
	}

	@Override
	public boolean enable() {
		boolean isHasDataset = false;
		Datasources datasources = Application.getActiveApplication().getWorkspace().getDatasources();
		for (int i = 0; i < datasources.getCount(); i++) {
			Datasource datasource = datasources.get(i);
			if (datasource.getDatasets().getCount() > 0) {
				isHasDataset = true;
				break;
			}
		}
		if (!isHasDataset) {
			return false;
		}
		boolean isHasOpenedDatasource = false;
		for (int i = 0; i < datasources.getCount(); i++) {
			Datasource datasource = datasources.get(i);
			if (datasource.isOpened() && !datasource.isReadOnly()) {
				isHasOpenedDatasource = true;
				break;
			}
		}
		if (!isHasOpenedDatasource) {
			return false;
		}
		return true;
	}
}
