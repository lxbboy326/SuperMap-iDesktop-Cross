package com.supermap.desktop.CtrlAction.LayerSetting;

import com.supermap.desktop.Application;
import com.supermap.desktop.FormMap;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.LayersTree;
import com.supermap.desktop.ui.controls.NodeDataType;
import com.supermap.desktop.ui.controls.TreeNodeData;
import com.supermap.mapping.LayerGroup;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by lixiaoyao on 2017/10/11.
 */
public class CtrlActionCreateLayerGroup extends CtrlAction{

	private TreeNodeData selectedNodeData=null;

	public CtrlActionCreateLayerGroup(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		IForm iForm=Application.getActiveApplication().getActiveForm();
		if (this.selectedNodeData!=null && this.selectedNodeData.getData() instanceof LayerGroup &&
				iForm!=null && iForm instanceof FormMap){
			FormMap formMap=(FormMap)iForm;
			String layerGroupName=formMap.getMapControl().getMap().getLayers().getAvailableCaption("LayerGroup");
			LayerGroup layerGroup = (LayerGroup) this.selectedNodeData.getData();
			layerGroup.addGroup(layerGroupName);
			LayersTree layersTree = UICommonToolkit.getLayersManager().getLayersTree();
			layersTree.expandRow(layersTree.getMaxSelectionRow());
			int selectRow=layersTree.getMaxSelectionRow()+layerGroup.getCount();
			layersTree.setSelectionRow(selectRow);
			layersTree.startEditingAtPath(layersTree.getPathForRow(selectRow));

		}

	}

	@Override
	public boolean enable() {
		boolean enable=false;
		LayersTree layersTree = UICommonToolkit.getLayersManager().getLayersTree();
		if (layersTree!=null &&layersTree.getSelectionCount()==1){
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) layersTree.getLastSelectedPathComponent();
			this.selectedNodeData = (TreeNodeData) selectedNode.getUserObject();
			if (this.selectedNodeData.getType()== NodeDataType.LAYER_GROUP){
				enable=true;
			}
		}
		if (!enable){
			this.selectedNodeData=null;
		}
		return enable;
	}
}
