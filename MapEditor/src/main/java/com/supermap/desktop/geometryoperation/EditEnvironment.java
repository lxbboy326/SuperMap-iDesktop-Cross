package com.supermap.desktop.geometryoperation;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.core.MouseButtons;
import com.supermap.desktop.event.*;
import com.supermap.desktop.geometry.Abstract.*;
import com.supermap.desktop.geometry.Implements.DGeometryFactory;
import com.supermap.desktop.geometryoperation.editor.IEditor;
import com.supermap.desktop.geometryoperation.editor.NullEditor;
import com.supermap.desktop.utilities.ListUtilities;
import com.supermap.desktop.utilities.MapUtilities;
import com.supermap.mapping.*;
import com.supermap.ui.*;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

//import com.supermap.ui.GeometryModifiedListener;

// @formatter:off

/**
 * 1.需要与地图交互的编辑
 * 2.选中之后，点击执行出结果的编辑
 * 地图窗口任何时候只允许存在一种编辑状态
 *
 * @author highsad
 */
// @formatter:on
public class EditEnvironment {

	private IFormMap formMap;
	private EditProperties properties = new EditProperties();
	private IEditor editor = NullEditor.INSTANCE;
	private boolean isInitialAction = false; // 某些编辑功能需要搭配 MapControl 的 Action 使用，这时候不需要执行一些 ActionChanged 的回调方法
	private boolean isMiddleMousePressed = false;

	private static final int MAX_RECORD_COUNT = 2000;// 设置批量更新的最大提交数量
	private static final int GEOMETRY_CONVERT_TO_SEGMENT = 120;// 设置线面等对象转换时的segment

	private IEditModel editModel;
	private IEditController editController = NullEditController.instance();

	private FormActivatedListener formActivatedListener = new FormActivatedListener() {
		@Override
		public void formActivated(FormActivatedEvent e) {
			EditEnvironment.this.editController.formActivated(EditEnvironment.this, e);
		}
	};

	private FormDeactivatedListener formDeactivatedListener = new FormDeactivatedListener() {
		@Override
		public void formDeactivated(FormDeactivatedEvent e) {
			EditEnvironment.this.editController.formDeactivated(EditEnvironment.this, e);
		}
	};

	private DockbarClosedListener dockbarClosedListener = new DockbarClosedListener() {
		@Override
		public void dockbarClosed(DockbarClosedEvent e) {
			EditEnvironment.this.editController.dockbarClosed(EditEnvironment.this, e);
		}
	};

	private MouseListener mouseListener = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseButtons.MIDDLE_BUTTON) {
				EditEnvironment.this.isMiddleMousePressed = false;
			}

			EditEnvironment.this.editController.mouseReleased(EditEnvironment.this, e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseButtons.MIDDLE_BUTTON) {
				EditEnvironment.this.isMiddleMousePressed = true;
			}

			EditEnvironment.this.editController.mousePressed(EditEnvironment.this, e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			EditEnvironment.this.editController.mouseExited(EditEnvironment.this, e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			EditEnvironment.this.editController.mouseEntered(EditEnvironment.this, e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			EditEnvironment.this.editController.mouseClicked(EditEnvironment.this, e);
		}
	};
	private MouseMotionListener mouseMotionListener = new MouseMotionListener() {

		@Override
		public void mouseMoved(MouseEvent e) {
			EditEnvironment.this.editController.mouseMoved(EditEnvironment.this, e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	};
	private KeyListener keyListener = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {
			EditEnvironment.this.editController.keyTyped(EditEnvironment.this, e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			EditEnvironment.this.editController.keyReleased(EditEnvironment.this, e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				activateEditor(NullEditor.INSTANCE);
			}

			EditEnvironment.this.editController.keyPressed(EditEnvironment.this, e);
		}
	};
	private ActionChangedListener actionChangedListener = new ActionChangedListener() {

		@Override
		public void actionChanged(ActionChangedEvent arg0) {
			if (!EditEnvironment.this.isInitialAction && isMiddleMousePressed && arg0.getOldAction() != Action.PAN) {
				activateEditor(NullEditor.INSTANCE);
			}

			EditEnvironment.this.editController.actionChanged(EditEnvironment.this, arg0);
		}
	};
	private GeometrySelectedListener geometrySelectedListener = new GeometrySelectedListener() {

		@Override
		public void geometrySelected(GeometrySelectedEvent arg0) {
			EditEnvironment.this.editController.geometrySelected(EditEnvironment.this, arg0);
		}
	};
	private GeometrySelectChangedListener geometrySelectChangedListener = new GeometrySelectChangedListener() {

		@Override
		public void geometrySelectChanged(GeometrySelectChangedEvent arg0) {
			EditEnvironment.this.geometrySelectChanged(arg0);
			EditEnvironment.this.editController.geometrySelectChanged(EditEnvironment.this, arg0);
		}
	};
	private LayerEditableChangedListener layerEditableChangedListener = new LayerEditableChangedListener() {

		@Override
		public void editableChanged(LayerEditableChangedEvent arg0) {
			EditEnvironment.this.editableChanged(arg0);
		}
	};

	/**
	 * @author lixiaoyao
	 * @description 新增对选中的对象进行增删移动时，进行监听
	 */
	private GeometryModifiedListener geometryModifiedListener = new GeometryModifiedListener() {

		@Override
		public void geometryModified(GeometryEvent arg0) {
			EditEnvironment.this.editController.geometryModified(EditEnvironment.this, arg0);
		}
	};

	private LayerRemovedListener layerRemovedListener = new LayerRemovedListener() {

		@Override
		public void layerRemoved(LayerRemovedEvent arg0) {
			EditEnvironment.this.layerRemoved(arg0);
		}
	};
	private MapClosedListener mapClosedListener = new MapClosedListener() {

		@Override
		public void mapClosed(MapClosedEvent arg0) {
			EditEnvironment.this.mapClosed(arg0);
		}
	};
	private MapOpenedListener mapOpenedListener = new MapOpenedListener() {

		@Override
		public void mapOpened(MapOpenedEvent arg0) {
			EditEnvironment.this.mapOpened(arg0);
		}
	};
	private RedoneListener redoneListener = new RedoneListener() {

		@Override
		public void redone(EventObject arg0) {
			EditEnvironment.this.editController.redone(EditEnvironment.this, arg0);
		}
	};
	private UndoneListener undoneListener = new UndoneListener() {

		@Override
		public void undone(EventObject arg0) {
			EditEnvironment.this.editController.undone(EditEnvironment.this, arg0);
		}
	};
	private TrackingListener trackingListener = new TrackingListener() {

		@Override
		public void tracking(TrackingEvent arg0) {
			EditEnvironment.this.editController.tracking(EditEnvironment.this, arg0);
		}
	};
	private TrackedListener trackedListener = new TrackedListener() {

		@Override
		public void tracked(TrackedEvent arg0) {
			EditEnvironment.this.editController.tracked(EditEnvironment.this, arg0);
		}
	};

	private EditEnvironment(IFormMap formMap) {
		this.formMap = formMap;

		if (this.formMap != null) {
			registerEvents();
			// 工具条上的下拉按钮在点开的时候才会构造，在那之前对地图的操作都没有记录，因此在这里初始化一下。
			geometryStatusChange();
			layersStatusChange();
		}
	}

	private void registerEvents() {
		Application.getActiveApplication().getMainFrame().getDockbarManager().addDockbarClosedListener(this.dockbarClosedListener);

		this.formMap.addFormActivatedListener(this.formActivatedListener);
		this.formMap.addFormDeactivatedListener(this.formDeactivatedListener);
		this.formMap.getMapControl().addMouseListener(this.mouseListener);
		this.formMap.getMapControl().addMouseMotionListener(this.mouseMotionListener);
		this.formMap.getMapControl().addKeyListener(this.keyListener);
		this.formMap.getMapControl().addActionChangedListener(this.actionChangedListener);

		this.formMap.getMapControl().addRedoneListener(this.redoneListener);
		this.formMap.getMapControl().addUndoneListener(this.undoneListener);
		this.formMap.getMapControl().addTrackingListener(this.trackingListener);
		this.formMap.getMapControl().addTrackedListener(this.trackedListener);
		// 选中对象状态改变
		this.formMap.getMapControl().addGeometrySelectChangedListener(this.geometrySelectChangedListener);
		this.formMap.getMapControl().addGeometrySelectedListener(this.geometrySelectedListener);
		//选中的对象进行删除节点或拖动节点
		this.formMap.getMapControl().addGeometryModifiedListener(this.geometryModifiedListener);
		// 图层可编辑状态改变
		this.formMap.getMapControl().getMap().getLayers().addLayerEditableChangedListener(this.layerEditableChangedListener);
		this.formMap.getMapControl().getMap().getLayers().addLayerRemovedListener(this.layerRemovedListener);
		this.formMap.getMapControl().getMap().addMapClosedListener(this.mapClosedListener);
		this.formMap.getMapControl().getMap().addMapOpenedListener(this.mapOpenedListener);
	}

	private void unregisterEventsWhenClosing() {
		this.formMap.getMapControl().removeMouseListener(this.mouseListener);
		this.formMap.getMapControl().removeMouseMotionListener(this.mouseMotionListener);
		this.formMap.getMapControl().removeKeyListener(this.keyListener);
		this.formMap.getMapControl().removeActionChangedListener(this.actionChangedListener);

		this.formMap.getMapControl().removeRedoneListener(this.redoneListener);
		this.formMap.getMapControl().removeUndoneListener(this.undoneListener);
		this.formMap.getMapControl().removeTrackingListener(this.trackingListener);
		this.formMap.getMapControl().removeTrackedListener(this.trackedListener);
		// 选中对象状态改变
		this.formMap.getMapControl().removeGeometrySelectChangedListener(this.geometrySelectChangedListener);
		this.formMap.getMapControl().removeGeometrySelectedListener(this.geometrySelectedListener);
		//选中的对象进行删除节点或拖动节点
		this.formMap.getMapControl().removeGeometryModifiedListener(this.geometryModifiedListener);
		// 图层可编辑状态改变
		this.formMap.getMapControl().getMap().getLayers().removeLayerEditableChangedListener(this.layerEditableChangedListener);
		this.formMap.getMapControl().getMap().getLayers().removeLayerRemovedListener(this.layerRemovedListener);
		this.formMap.getMapControl().getMap().removeMapClosedListener(this.mapClosedListener);
		this.formMap.getMapControl().getMap().removeMapOpenedListener(this.mapOpenedListener);
	}

	private void unregisterEventsWhenClosed() {
		Application.getActiveApplication().getMainFrame().getDockbarManager().removeDockbarClosedListener(this.dockbarClosedListener);
		this.formMap.removeFormActivatedListener(this.formActivatedListener);
		this.formMap.removeFormDeactivatedListener(this.formDeactivatedListener);
	}

	public IEditModel getEditModel() {
		return this.editModel;
	}

	public void setEditModel(IEditModel editModel) {
		this.editModel = editModel;
	}

	public void setEditController(IEditController editController) {
		this.editController = editController;
	}

	/**
	 * 获取所有图层
	 *
	 * @return
	 */
	public List<Layer> getAllLayers() {
		return MapUtilities.getLayers(this.formMap.getMapControl().getMap());
	}

	public IFormMap getFormMap() {
		return this.formMap;
	}

	public MapControl getMapControl() {
		return this.formMap.getMapControl();
	}

	public Map getMap() {
		return this.formMap.getMapControl().getMap();
	}

	public Layer getActiveEditableLayer() {
		return this.formMap.getMapControl().getActiveEditableLayer();
	}

	public Layer[] getEditableLayers() {
		return this.formMap.getMapControl().getEditableLayers();
	}

	public EditProperties getEditProperties() {
		return this.properties;
	}

	public IEditor getEditor() {
		return this.editor;
	}

	public void activateEditor(IEditor editor) {
		try {
			this.isInitialAction = true;
			this.editor.deactivate(this);

			// 点击一次开启功能，再次点击该功能结束
			if (this.editor != NullEditor.INSTANCE && this.editor == editor) {
				this.editor = NullEditor.INSTANCE;
			} else {
				this.editor = editor;
			}
			this.editor.activate(this);
		} finally {
			this.isInitialAction = false;
		}
	}

	public void stopEditor() {
		activateEditor(NullEditor.INSTANCE);
	}

	/**
	 * 创建一个实例，由此可以保证实例中的 formMap 必定有效
	 *
	 * @param formMap
	 * @return
	 */
	public static EditEnvironment createInstance(IFormMap formMap) {
		if (formMap == null) {
			throw new IllegalArgumentException("formMap can not be null.");
		}

		return new EditEnvironment(formMap);
	}

	public void geometrySelectChanged(GeometrySelectChangedEvent arg0) {
		geometryStatusChange();
	}

	public void editableChanged(LayerEditableChangedEvent arg0) {
		geometryStatusChange();
		layersStatusChange();
	}

	public void layerRemoved(LayerRemovedEvent arg0) {
		geometryStatusChange();
		layersStatusChange();
	}

	/**
	 * 获取当前地图窗口中，所需要的状态数据
	 */
	private void geometryStatusChange() {
		try {
			// 选中对象数目
			resetGeometryStatus();
			if (!Application.getActiveApplication().getMainFrame().getFormManager().isContain(formMap)) {
				return;
			}

			List<Layer> layers = MapUtilities.getLayers(this.formMap.getMapControl().getMap());
			for (int i = 0; i < layers.size(); i++) {
				Layer layer = layers.get(i);

				if (layer.getDataset() == null) {
					continue;
				}

				if (layer.getSelection() == null || layer.getSelection().getCount() == 0) {
					continue;
				}

				if (!this.properties.getSelectedDatasetTypes().contains(layer.getDataset().getType())) {
					this.properties.getSelectedDatasetTypes().add(layer.getDataset().getType());
				}
				this.properties.getSelectedLayers().add(layer);
				statisticGeometryData(layer);
			}
			//  文本默认风格设置 2017.1.17李逍遥 10   共计part10
			setDefaultTextStyle();//    get当前选中的可编辑的文本对象风格
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	/**
	 * 获取当前地图窗口中，选中的可编辑的文本对象的风格
	 */
	private void setDefaultTextStyle(){
		if (!Application.getActiveApplication().getMainFrame().getFormManager().isContain(formMap)) {
			return;
		}
		Layer layer=this.formMap.getMapControl().getActiveEditableLayer();
		if ( layer!=null && layer.getDataset() != null &&(layer.getDataset().getType()== DatasetType.CAD || layer.getDataset().getType()==DatasetType.TEXT)){
			if (layer.getSelection() != null && layer.getSelection().getCount() != 0) {
				Recordset recordset=layer.getSelection().toRecordset();
				for (int i=0;i<recordset.getRecordCount();i++){
					Geometry geometry=recordset.getGeometry();
					if (geometry.getType()==GeometryType.GEOTEXT || geometry.getType()==GeometryType.GEOTEXT3D ){//  不管选中多少个文本对象，始终取第一个文本对象设置给默认文本风格
						this.formMap.setDefaultTextStyle(((GeoText)geometry).getTextStyle().clone());
						this.formMap.setDefaultTextRotationAngle(((GeoText)geometry).getPart(0).getRotation());
						break;
					}
				}
			}
		}
	}

	/**
	 * 统计指定图层 Geometry 的状态
	 *
	 * @param layer
	 */
	private void statisticGeometryData(Layer layer) {
		Recordset recordset = null;

		try {
			ArrayList<Class<?>> features = new ArrayList<>();
			ArrayList<Class<?>> typeFeatures = new ArrayList<>();
			ArrayList<GeometryType> types = new ArrayList<>();
			int editableSelectedMaxPartCount = 0;

			Selection selection = layer.getSelection();
			recordset = selection.toRecordset();

			recordset.moveFirst();
			while (!recordset.isEOF()) {
				Geometry geometry = recordset.getGeometry();

				if (geometry == null) {
					recordset.moveNext();
				}

				try {
					if (null != geometry) {
						GeometryType type = geometry.getType();
						if (!types.contains(type)) {
							types.add(type);
						}

						IGeometry dGeometry = DGeometryFactory.create(geometry);

						Class<?> typeFeature = getTypeFeature(dGeometry);
						if (!typeFeatures.contains(typeFeature)) {
							typeFeatures.add(typeFeature);
						}
						if (dGeometry instanceof IMultiPartFeature<?>) {
							editableSelectedMaxPartCount = Math.max(editableSelectedMaxPartCount, ((IMultiPartFeature<?>) dGeometry).getPartCount());
						}
					}
				} finally {
					if (geometry != null) {
						geometry.dispose();
					}
				}
				recordset.moveNext();
			}

			this.properties.setSelectedGeometryCount(this.properties.getSelectedGeometryCount() + selection.getCount());
			ListUtilities.addArraySingle(this.properties.getSelectedGeometryTypeFeatures(), typeFeatures.toArray(new Class<?>[typeFeatures.size()]));
			ListUtilities.addArraySingle(this.properties.getSelectedGeometryTypes(), types.toArray(new GeometryType[types.size()]));
			if (layer.isEditable()) {
				this.properties.setEditableSelectedGeometryCount(this.properties.getEditableSelectedGeometryCount() + selection.getCount());
				this.properties.setEditableSelectedMaxPartCount(Math.max(this.properties.getEditableSelectedMaxPartCount(), editableSelectedMaxPartCount));
				ListUtilities
						.addArraySingle(this.properties.getEditableSelectedGeometryTypeFeatures(), typeFeatures.toArray(new Class<?>[typeFeatures.size()]));
				ListUtilities.addArraySingle(this.properties.getEditableSelectedGeometryTypes(), types.toArray(new GeometryType[types.size()]));
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}
		}
	}

	private Class<?> getTypeFeature(IGeometry dGeometry) {
		if (dGeometry instanceof IPointFeature) {
			return IPointFeature.class;
		} else if (dGeometry instanceof ILineFeature) {
			return ILineFeature.class;
		} else if (dGeometry instanceof ILineMFeature) {
			return ILineMFeature.class;
		} else if (dGeometry instanceof IRegionFeature) {
			return IRegionFeature.class;
		} else if (dGeometry instanceof ITextFeature) {
			return ITextFeature.class;
		} else if (dGeometry instanceof ICompoundFeature) {
			return ICompoundFeature.class;
		} else if (dGeometry instanceof ILine3DFeature) {
			return ILine3DFeature.class;
		} else if (dGeometry instanceof IRegion3DFeature) {
			return IRegion3DFeature.class;
		}
		return INormalFeature.class;
	}

	private void resetGeometryStatus() {
		// 选中对象数目
		this.properties.setSelectedGeometryCount(0);
		this.properties.setEditableSelectedGeometryCount(0);
		this.properties.setEditableSelectedMaxPartCount(0);
		this.properties.getSelectedDatasetTypes().clear();
		this.properties.getSelectedLayers().clear();
		this.properties.getSelectedGeometryTypes().clear();
		this.properties.getEditableSelectedGeometryTypes().clear();
		this.properties.getSelectedGeometryFeatures().clear();
		this.properties.getEditableSelectedGeometryFeatures().clear();
		this.properties.getSelectedGeometryTypeFeatures().clear();
		this.properties.getEditableSelectedGeometryTypeFeatures().clear();
	}

	private void layersStatusChange() {
		try {
			resetLayersStatus();

			if (!Application.getActiveApplication().getMainFrame().getFormManager().isContain(formMap)) {
				return;
			}

			for (int i = 0; i < this.formMap.getMapControl().getEditableLayers().length; i++) {
				Layer layer = this.formMap.getMapControl().getEditableLayers()[i];
				if (layer.getDataset() != null && !this.properties.getEditableDatasetTypes().contains(layer.getDataset().getType())) {
					this.properties.getEditableDatasetTypes().add(layer.getDataset().getType());
				}
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
	}

	private void resetLayersStatus() {
		this.properties.getEditableDatasetTypes().clear();
	}

	/**
	 * 直接打开地图会导致 Layers 对象的改变，因此需要重新处理一下 Layers 相关事件的监听
	 */
	public void mapOpened(MapOpenedEvent arg0) {
		this.formMap.getMapControl().getMap().getLayers().addLayerEditableChangedListener(this.layerEditableChangedListener);
		this.formMap.getMapControl().getMap().getLayers().addLayerRemovedListener(this.layerRemovedListener);

		// 直接打开地图导致 Layers 对象改变之后，要重新处理一下地图的状态数据
		geometryStatusChange();
		layersStatusChange();
	}

	public void mapClosed(MapClosedEvent arg0) {
		this.formMap.getMapControl().getMap().getLayers().removeLayerEditableChangedListener(this.layerEditableChangedListener);
		this.formMap.getMapControl().getMap().getLayers().removeLayerRemovedListener(this.layerRemovedListener);
	}

	/**
	 * @author lixiaoyao
	 * @desciption 获取批量更新的最大提交数目；获取geometry转换的segment
	 */
	public int getMaxRecordCount() {
		return MAX_RECORD_COUNT;
	}

	public int getGeometryConverToSegment() {
		return GEOMETRY_CONVERT_TO_SEGMENT;
	}

	/**
	 * 预清理。有一部分的资源需要在 Form 关闭前清理
	 * 比如 mapControl 的相关事件，因为 FormMap 关闭前会移除 MapControl 的关联
	 */
	public void preClear() {
		unregisterEventsWhenClosing();
	}

	public void clear() {
		unregisterEventsWhenClosed();
		this.formMap = null;
		this.properties.clear();
		this.editor = NullEditor.INSTANCE;
		this.isInitialAction = false; // 某些编辑功能需要搭配 MapControl 的 Action 使用，这时候不需要执行一些 ActionChanged 的回调方法
		this.isMiddleMousePressed = false;
		this.editModel = null;
		this.editController = NullEditController.instance();
	}
}
