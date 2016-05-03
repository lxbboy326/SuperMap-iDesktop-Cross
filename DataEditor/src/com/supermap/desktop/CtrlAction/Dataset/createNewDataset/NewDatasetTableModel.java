package com.supermap.desktop.CtrlAction.Dataset.createNewDataset;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.CtrlAction.Dataset.AddToWindowMode;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.dataeditor.DataEditorProperties;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.utilties.*;
import com.supermap.mapping.Map;
import com.supermap.ui.Action;

import javax.swing.table.DefaultTableModel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author XiaJT
 */
public class NewDatasetTableModel extends DefaultTableModel {

	private final String[] columnNames = new String[]{
			CommonProperties.getString("String_ColumnHeader_Index"),
			CommonProperties.getString("String_ColumnHeader_TargetDatasource"),
			DataEditorProperties.getString("String_CreateType"),
			DataEditorProperties.getString("String_ColumnTitle_DtName"),
			CommonProperties.getString("String_ColumnHeader_EncodeType"),
			DataEditorProperties.getString("String_Charset"),
			DataEditorProperties.getString("String_DataGridViewComboBoxColumn_Name")
	};
	private final boolean[] isColumnEditable = new boolean[]{
			false, true, true, true, true, true, true
	};

	public static final int COLUMN_INDEX_INDEX = 0;
	public static final int COLUMN_INDEX_TARGET_DATASOURCE = 1;
	public static final int COLUMN_INDEX_DatasetType = 2;
	public static final int COLUMN_INDEX_DatasetName = 3;
	public static final int COLUMN_INDEX_EncodeType = 4;
	public static final int COLUMN_INDEX_Charset = 5;
	public static final int COLUMN_INDEX_WindowMode = 6;

	private ArrayList<NewDatasetBean> datasetBeans = new ArrayList<>();

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COLUMN_INDEX_INDEX) {
			return String.class;
		} else if (columnIndex == COLUMN_INDEX_TARGET_DATASOURCE) {
			return Datasource.class;
		} else if (columnIndex == COLUMN_INDEX_DatasetType) {
			return String.class;
		} else if (columnIndex == COLUMN_INDEX_DatasetName) {
			return String.class;
		} else if (columnIndex == COLUMN_INDEX_EncodeType) {
			return String.class;
		} else if (columnIndex == COLUMN_INDEX_Charset) {
			return String.class;
		} else if (columnIndex == COLUMN_INDEX_WindowMode) {
			return String.class;
		}
		return String.class;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == COLUMN_INDEX_INDEX) {
			return row + 1;
		} else if (column == COLUMN_INDEX_TARGET_DATASOURCE) {
			return datasetBeans.get(row).getDatasource();
		} else if (column == COLUMN_INDEX_DatasetType) {
			return DatasetTypeUtilties.toString(datasetBeans.get(row).getDatasetType());
		} else if (column == COLUMN_INDEX_DatasetName) {
			return datasetBeans.get(row).getDatasetName();
		} else if (column == COLUMN_INDEX_EncodeType) {
			return EncodeTypeUtilties.toString(datasetBeans.get(row).getEncodeType());
		} else if (column == COLUMN_INDEX_Charset) {
			return CharsetUtilties.toString(datasetBeans.get(row).getCharset());
		} else if (column == COLUMN_INDEX_WindowMode) {
			return datasetBeans.get(row).getAddToWindowMode();
		}
		throw new UnsupportedOperationException("column out of index");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return isColumnEditable[column];
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (column == COLUMN_INDEX_INDEX) {
			throw new UnsupportedOperationException("index can't change");
		} else if (column == COLUMN_INDEX_TARGET_DATASOURCE) {
			datasetBeans.get(row).setDatasource((Datasource) aValue);
			checkCurrentName(row);
		} else if (column == COLUMN_INDEX_DatasetType) {
			datasetBeans.get(row).setDatasetType((DatasetType) aValue);
			checkCurrentName(row);
		} else if (column == COLUMN_INDEX_DatasetName) {
			setDatasetName(row, (String) aValue);
		} else if (column == COLUMN_INDEX_EncodeType) {
			datasetBeans.get(row).setEncodeType((EncodeType) aValue);
		} else if (column == COLUMN_INDEX_Charset) {
			datasetBeans.get(row).setCharset((Charset) aValue);
		} else if (column == COLUMN_INDEX_WindowMode) {
			datasetBeans.get(row).setAddToWindowMode((AddToWindowMode) aValue);
		}
	}

	private void checkCurrentName(int row) {
		String datasetName = datasetBeans.get(row).getDatasetName();
		if (StringUtilties.isNullOrEmpty(datasetName) || datasetName.contains(getDefaultDatasetName(datasetBeans.get(row).getDatasetType()))) {
			setValueAt("", row, COLUMN_INDEX_DatasetName);
		} else {
			setValueAt(datasetName, row, COLUMN_INDEX_DatasetName);
		}
	}

	private void setDatasetName(int row, String aValue) {
		if (StringUtilties.isNullOrEmpty(aValue)) {
			setDatasetName(row, getDefaultDatasetName(datasetBeans.get(row).getDatasetType()));
		}

		List<String> datasetNames = new ArrayList<>();
		for (int i = 0; i < datasetBeans.size(); i++) {
			if (i != row) {
				datasetNames.add(datasetBeans.get(i).getDatasetName());
			}
		}
		datasetBeans.get(row).setDatasetName(getUsableDatasetName(aValue, datasetBeans.get(row).getDatasource(), datasetNames, 0));

		if (row == datasetBeans.size() - 1) {
			addEmptyRow();
		}

	}

	private String getUsableDatasetName(String source, Datasource datasource, List<String> datasetNames, int i) {
		if (StringUtilties.isNullOrEmpty(source)) {
			throw new UnsupportedOperationException("name should not null");
		}
		String currentDataset = source;
		if (i != 0) {
			currentDataset = getComboDatasetName(currentDataset, i);
		}
		if (datasetNames.contains(currentDataset)) {
			i++;
			return getUsableDatasetName(source, datasource, datasetNames, i);
		}
		String tempDatasetName = datasource.getDatasets().getAvailableDatasetName(currentDataset);
		if (!tempDatasetName.equals(currentDataset)) {
			i++;
			return getUsableDatasetName(source, datasource, datasetNames, i);
		}
		return currentDataset;
	}

	private String getComboDatasetName(String aValue, int i) {
		return MessageFormat.format("{0}_{1}", aValue, i);
	}

	private String getDefaultDatasetName(DatasetType datasetType) {
		String newDatasetName = "";
		if (datasetType == DatasetType.POINT) {
			newDatasetName = "New_Point";
		} else if (datasetType == DatasetType.LINE) {
			newDatasetName = "New_Line";
		} else if (datasetType == DatasetType.REGION) {
			newDatasetName = "New_Region";
		} else if (datasetType == DatasetType.TEXT) {
			newDatasetName = "New_Text";
		} else if (datasetType == DatasetType.CAD) {
			newDatasetName = "New_CAD";
		} else if (datasetType == DatasetType.TABULAR) {
			newDatasetName = "New_Tabular";
		} else if (datasetType == DatasetType.POINT3D) {
			newDatasetName = "New_Point3D";
		} else if (datasetType == DatasetType.LINE3D) {
			newDatasetName = "New_Line3D";
		} else if (datasetType == DatasetType.REGION3D) {
			newDatasetName = "New_Region3D";
		} else if (datasetType == DatasetType.PARAMETRICLINE) {
			newDatasetName = "New_ParametricLine";
		} else if (datasetType == DatasetType.PARAMETRICREGION) {
			newDatasetName = "New_ParametricRegion";
		} else if (datasetType == DatasetType.IMAGECOLLECTION) {
			newDatasetName = "New_ImageCollection";
		}

		return newDatasetName;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		return datasetBeans.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public void removeRow(int row) {
		this.datasetBeans.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public void addEmptyRow() {
		NewDatasetBean newDatasetBean = new NewDatasetBean();
		if (datasetBeans.size() > 0) {
			newDatasetBean.setDatasetType(datasetBeans.get(datasetBeans.size() - 1).getDatasetType());
			newDatasetBean.setDatasource(datasetBeans.get(datasetBeans.size() - 1).getDatasource());
			newDatasetBean.setAddToWindowMode(datasetBeans.get(datasetBeans.size() - 1).getAddToWindowMode());
		}
		datasetBeans.add(newDatasetBean);
	}

	public void createDatasets() {
		int successCount = 0;
		List<Dataset> addToCurrentWindow = new ArrayList<>();
		List<Dataset> addToNewWindow = new ArrayList<>();
		for (NewDatasetBean datasetBean : datasetBeans) {
			if (datasetBean.createDataset()) {
				successCount++;
				if (datasetBean.getAddToWindowMode() != AddToWindowMode.NONEWINDOW) {
					Dataset dataset = datasetBean.getDatasource().getDatasets().get(datasetBean.getDatasetName());
					if (datasetBean.getAddToWindowMode() == AddToWindowMode.CURRENTWINDOW) {
						addToCurrentWindow.add(dataset);
					} else {
						addToNewWindow.add(dataset);
					}
				}
			}
		}
		String information = MessageFormat.format(DataEditorProperties.getString("String_CreateNewDT_Message"), datasetBeans.size() - 1, successCount,
				datasetBeans.size() - 1 - successCount);
		Application.getActiveApplication().getOutput().output(information);

		if (!addToCurrentWindow.isEmpty()) {
			IFormMap formMap = (IFormMap) Application.getActiveApplication().getActiveForm();
			Map map = formMap.getMapControl().getMap();
			for (Dataset dataset : addToCurrentWindow) {
				if (dataset.getType() != DatasetType.TABULAR && dataset.getType() != DatasetType.TOPOLOGY) {
					map.getLayers().add(dataset, true);
				}

				map.refresh();
				UICommonToolkit.getLayersManager().setMap(map);
			}
		}

		if (!addToNewWindow.isEmpty()) {
			String name = MapUtilties
					.getAvailableMapName(MessageFormat.format("{0}@{1}", addToNewWindow.get(0).getName(), addToNewWindow.get(0).getDatasource().getAlias()), true);
			IFormMap formMap = (IFormMap) CommonToolkit.FormWrap.fireNewWindowEvent(WindowType.MAP, name);

			if (formMap != null) {
				Map map = formMap.getMapControl().getMap();
				for (Dataset dataset : addToNewWindow) {
					if (dataset.getType() != DatasetType.TABULAR && dataset.getType() != DatasetType.TOPOLOGY) {
						map.getLayers().add(dataset, true);
					}
				}

				map.refresh();
				UICommonToolkit.getLayersManager().setMap(map);

				formMap.getMapControl().setAction(Action.PAN);
			}
		}
	}
}
