package com.supermap.desktop.CtrlAction.transformationForm.CtrlAction;

import com.supermap.data.Transformation;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;

/**
 * @author XiaJT
 */
public class CtrlActionTransformInput extends CtrlAction {
	public CtrlActionTransformInput(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		Transformation transformation = new Transformation();
	}

	@Override
	public boolean enable() {
		return super.enable();
	}
}