package com.supermap.desktop.geometryoperation.CtrlAction;

import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.geometryoperation.editor.IEditor;
import com.supermap.desktop.geometryoperation.editor.RegionSplitByRegionEditor;
/**
 * @author lixiaoyao
 */
public class CtrlActionRegionSplitByRegion extends CtrlActionEditorBase{
	private RegionSplitByRegionEditor editor=new RegionSplitByRegionEditor();

	public CtrlActionRegionSplitByRegion(IBaseItem caller, IForm formClass)
	{
		super(caller,formClass);
	}
	@Override
	public  IEditor getEditor()
	{
		return  this.editor;
	}
}
