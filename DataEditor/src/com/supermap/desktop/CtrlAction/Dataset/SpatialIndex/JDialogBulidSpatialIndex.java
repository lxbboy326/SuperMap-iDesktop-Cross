package com.supermap.desktop.CtrlAction.Dataset.SpatialIndex;

import com.supermap.data.Dataset;
import com.supermap.data.Datasource;
import com.supermap.data.SpatialIndexInfo;
import com.supermap.data.SpatialIndexType;
import com.supermap.desktop.Application;
import com.supermap.desktop.CtrlAction.Dataset.Pyramid.JDialogDatasetChoosePyramidManager;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.controls.utilties.DatasetUtilties;
import com.supermap.desktop.dataeditor.DataEditorProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.ui.controls.CellRenders.TabelDatasourceCellRender;
import com.supermap.desktop.ui.controls.CellRenders.TableDatasetCellRender;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.ui.controls.SmDialog;
import com.supermap.desktop.utilties.SpatialIndexInfoUtilties;
import com.supermap.desktop.utilties.SpatialIndexTypeUtilties;
import com.supermap.desktop.utilties.TableUtilties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 重构空间索引界面
 *
 * @author XiaJT
 */
public class JDialogBulidSpatialIndex extends SmDialog {
	//region 成员变量
	// 工具条
	private JToolBar toolBar;
	private JButton buttonAdd;
	private JButton buttonSelectAll;
	private JButton buttonSelectInvert;
	private JButton buttonDelete;

	// 索引类型
	private JPanel panelIndexType;
	private JLabel labelIndexType;
	private JComboBox comboBoxIndexType;

	// 表
	private JScrollPane scrollPaneTable;
	private JTable tableDatasets;
	private SpatialIndexTableModel spatialIndexTableModel;

	Datasource datasource = Application.getActiveApplication().getWorkspace().getDatasources().get(0);
	JDialogDatasetChoosePyramidManager dialogDatasetChoosePyramidManager = new JDialogDatasetChoosePyramidManager(this, true, datasource, SpatialIndexTableModelBean.SUPPORT_DATASET_TYPES);

	private final int rowHeight = 23;

	/**
	 * 防止多次刷新
	 */
	private boolean key = true;
	// 右下的描述
	private JScrollPane scrollPaneDescribe;
	private JTextArea textAreaNull;
	private JTextArea textAreaRTree;
	private JTextArea textAreaQuadTree;
	private JPanelDynamicIndex panelDynamicIndex = new JPanelDynamicIndex();
	private JPanelGraphIndex panelGraphIndex = new JPanelGraphIndex();

	// 按钮
	private JPanel panelButton;
	private JButton buttonOk;
	private JButton buttonCancle;


	//endregion

	public JDialogBulidSpatialIndex() {
		initComponents();
		initLayout();
		addListeners();
		initComponentStates();
		initResources();
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
	}

	private void initComponents() {
		// 工具条
		this.toolBar = new JToolBar();
		this.buttonAdd = new JButton();
		this.buttonSelectAll = new JButton();
		this.buttonSelectInvert = new JButton();
		this.buttonDelete = new JButton();

		this.buttonAdd.setIcon(new ImageIcon(JDialogBulidSpatialIndex.class.getResource("/com/supermap/desktop/coreresources/ToolBar/Image_ToolButton_AddMap.png")));
		this.buttonSelectAll.setIcon(new ImageIcon(JDialogBulidSpatialIndex.class.getResource("/com/supermap/desktop/coreresources/ToolBar/Image_ToolButton_SelectAll.png")));
		this.buttonSelectInvert.setIcon(new ImageIcon(JDialogBulidSpatialIndex.class.getResource("/com/supermap/desktop/coreresources/ToolBar/Image_ToolButton_SelectInverse.png")));
		this.buttonDelete.setIcon(new ImageIcon(JDialogBulidSpatialIndex.class.getResource("/com/supermap/desktop/coreresources/ToolBar/Image_ToolButton_Delete.png")));
		// 索引类型
		this.panelIndexType = new JPanel();
		this.labelIndexType = new JLabel();
		this.comboBoxIndexType = new JComboBox();
		// 表
		this.scrollPaneTable = new JScrollPane();
		this.tableDatasets = new JTable();
		this.spatialIndexTableModel = new SpatialIndexTableModel();
		// 右下的描述
		this.scrollPaneDescribe = new JScrollPane();
		this.textAreaNull = new JTextArea();
		this.textAreaNull.setLineWrap(true);
		this.textAreaRTree = new JTextArea();
		this.textAreaRTree.setLineWrap(true);
		this.textAreaQuadTree = new JTextArea();
		this.textAreaQuadTree.setLineWrap(true);
		this.panelDynamicIndex = new JPanelDynamicIndex();
		this.panelGraphIndex = new JPanelGraphIndex();

		this.textAreaNull.setEditable(false);
		this.textAreaRTree.setEditable(false);
		this.textAreaQuadTree.setEditable(false);

		// 按钮栏
		this.panelButton = new JPanel();
		this.buttonOk = new JButton();
		this.buttonCancle = new JButton();
	}

	//region 初始化布局

	/**
	 * 初始化布局
	 */
	private void initLayout() {
		initToolBars();
		initPanelIndexType();
		initTableDatasets();
		initPanelDescribe();
		initPanelButton();

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridBagLayout());
		centerPanel.add(this.toolBar, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(3, 0).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 5));
		centerPanel.add(this.panelIndexType, new GridBagConstraintsHelper(1, 0, 1, 1).setWeight(0, 0).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 0));
		centerPanel.add(this.scrollPaneTable, new GridBagConstraintsHelper(0, 1, 1, 1).setWeight(3, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH).setInsets(0, 0, 5, 5));
		centerPanel.add(this.scrollPaneDescribe, new GridBagConstraintsHelper(1, 1, 1, 1).setWeight(0, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH).setInsets(0, 0, 5, 0));
		centerPanel.add(this.panelButton, new GridBagConstraintsHelper(0, 2, 2, 1).setWeight(1, 0).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH));

		this.setLayout(new GridBagLayout());
		this.add(centerPanel, new GridBagConstraintsHelper(0, 0, 1, 1).setInsets(10).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1));
	}

	/**
	 * 初始化工具条
	 */
	private void initToolBars() {
		this.toolBar.setFloatable(false);

		this.toolBar.add(this.buttonAdd);
		this.toolBar.add(getSeparator());
		this.toolBar.add(this.buttonSelectAll);
		this.toolBar.add(this.buttonSelectInvert);
		this.toolBar.add(getSeparator());
		this.toolBar.add(this.buttonDelete);
	}


	private void initPanelIndexType() {
		this.panelIndexType.setLayout(new GridBagLayout());
		this.panelIndexType.add(this.labelIndexType, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(0, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.CENTER).setInsets(0, 0, 0, 5));
		this.panelIndexType.add(this.comboBoxIndexType, new GridBagConstraintsHelper(1, 0, 1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER));
	}

	/**
	 * 获得工具条分隔符
	 *
	 * @return
	 */
	private JToolBar.Separator getSeparator() {
		JToolBar.Separator separator = new JToolBar.Separator();
		separator.setOrientation(SwingConstants.VERTICAL);
		return separator;
	}

	/**
	 * 初始化表格
	 */
	private void initTableDatasets() {
		this.tableDatasets.setRowHeight(rowHeight);
		this.scrollPaneTable.setViewportView(tableDatasets);
		this.tableDatasets.setModel(this.spatialIndexTableModel);
		this.tableDatasets.getColumnModel().getColumn(SpatialIndexTableModel.COLUMN_DATASET).setCellRenderer(new TableDatasetCellRender());
		this.tableDatasets.getColumnModel().getColumn(SpatialIndexTableModel.COLUMN_DATASOURCE).setCellRenderer(new TabelDatasourceCellRender());
		this.tableDatasets.getColumnModel().getColumn(SpatialIndexTableModel.COLUMN_DEAL_INDEX_TYPE).setCellEditor(new SpatialIndexTypeCellEditor(new JComboBox()));
	}

	/**
	 * 初始化索引类型描述
	 */
	private void initPanelDescribe() {
		Dimension size = new Dimension(200, 100);
		scrollPaneDescribe.setMinimumSize(size);
		scrollPaneDescribe.setPreferredSize(size);
		scrollPaneDescribe.setMaximumSize(size);
	}

	/**
	 * 初始化按钮面板
	 */
	private void initPanelButton() {
		this.panelButton.setLayout(new GridBagLayout());
		panelButton.add(buttonOk, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(99, 1).setAnchor(GridBagConstraints.EAST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 0, 5));
		panelButton.add(buttonCancle, new GridBagConstraintsHelper(1, 0, 1, 1).setWeight(1, 1).setAnchor(GridBagConstraints.EAST).setFill(GridBagConstraints.NONE));
	}

	//endregion


	//region 添加监听事件
	private void addListeners() {
		this.buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAddClicked();
			}
		});

		this.buttonSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableDatasets.selectAll();
			}
		});

		this.buttonSelectInvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableUtilties.invertSelection(tableDatasets);
			}
		});

		this.buttonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = tableDatasets.getSelectedRow();
				int[] selectedRows = tableDatasets.getSelectedRows();
				// 先置空不然移除时会抛数组越界移除
				tableDatasets.clearSelection();
				spatialIndexTableModel.removeDatasets(selectedRows);
				if (tableDatasets.getRowCount() > selectedRow && selectedRow != -1) {
					tableDatasets.setRowSelectionInterval(selectedRow, selectedRow);
				} else if (tableDatasets.getRowCount() > 0) {
					tableDatasets.setRowSelectionInterval(tableDatasets.getRowCount() - 1, tableDatasets.getRowCount() - 1);
				}
			}
		});

		this.tableDatasets.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				checkButtonStates();
			}
		});

		spatialIndexTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				checkButtonStates();
			}
		});


		MouseAdapter openDatasetChooseMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 &&
						(tableDatasets.rowAtPoint(e.getPoint()) == -1 || tableDatasets.rowAtPoint(e.getPoint()) > tableDatasets.getRowCount() - 1 || tableDatasets.columnAtPoint(e.getPoint()) != SpatialIndexTableModel.COLUMN_DEAL_INDEX_TYPE)) {
					buttonAddClicked();
				}
			}
		};
		this.tableDatasets.addMouseListener(openDatasetChooseMouseListener);
		this.scrollPaneTable.addMouseListener(openDatasetChooseMouseListener);
		this.buttonOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDialogResult(DialogResult.OK);
				buttonOkClick();
				JDialogBulidSpatialIndex.this.dispose();
			}
		});
		this.buttonCancle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDialogResult(DialogResult.CANCEL);
				JDialogBulidSpatialIndex.this.dispose();
			}
		});
		this.comboBoxIndexType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && key) {

					try {
						key = false;
						setTableValues(comboBoxIndexType.getSelectedItem());
					} finally {
						key = true;
					}
					checkScrollPanelDescribe();
				}
			}
		});
	}

	private void buttonAddClicked() {
		if (dialogDatasetChoosePyramidManager.showDialog() == DialogResult.OK) {
			int preRowCount = tableDatasets.getRowCount();
			spatialIndexTableModel.addDatasets(dialogDatasetChoosePyramidManager.getSelectedDatasets());
			int currentRowCount = tableDatasets.getRowCount();
			if (currentRowCount > preRowCount) {
				tableDatasets.setRowSelectionInterval(preRowCount, currentRowCount - 1);
				tableDatasets.scrollRectToVisible(tableDatasets.getCellRect(currentRowCount - 1, 0, true));
			}
		}
	}

	private void checkButtonStates() {
		checkButtonSelectAllAndInvertAndOkState();
		checkButtonDeleteState();
		checkComboboxIndexTypeState();
	}

	private void checkButtonSelectAllAndInvertAndOkState() {
		if (tableDatasets.getRowCount() > 0 != buttonSelectAll.isEnabled()) {
			buttonSelectAll.setEnabled(tableDatasets.getRowCount() > 0);
			buttonSelectInvert.setEnabled(tableDatasets.getRowCount() > 0);
			buttonOk.setEnabled(tableDatasets.getRowCount() > 0);
		}
	}

	private void checkButtonDeleteState() {
		if (tableDatasets.getSelectedRows().length > 0 != buttonDelete.isEnabled()) {
			buttonDelete.setEnabled(tableDatasets.getSelectedRows().length > 0);
		}
	}

	private void checkComboboxIndexTypeState() {
		resetComboboxIndexTypeModel();
		resetComboboxIndexTypeSelectItem();
		checkScrollPanelDescribe();
	}

	private void resetComboboxIndexTypeModel() {
		int[] selectedRows = tableDatasets.getSelectedRows();
		java.util.List<Dataset> datasetList = new ArrayList<>();
		for (int selectedRow : selectedRows) {
			datasetList.add((Dataset) tableDatasets.getValueAt(selectedRow, SpatialIndexTableModel.COLUMN_DATASET));
		}
		comboBoxIndexType.setModel(new DefaultComboBoxModel(SpatialIndexTypeUtilties.getSupportSpatialIndexTypes(datasetList)));
	}

	private void resetComboboxIndexTypeSelectItem() {
		String currentSpatialIndexType = null;
		int[] selectedRows = tableDatasets.getSelectedRows();
		for (int selectedRow : selectedRows) {
			if (currentSpatialIndexType == null) {
				currentSpatialIndexType = (String) tableDatasets.getValueAt(selectedRow, SpatialIndexTableModel.COLUMN_DEAL_INDEX_TYPE);
			} else if (!currentSpatialIndexType.equals((String) tableDatasets.getValueAt(selectedRow, SpatialIndexTableModel.COLUMN_DEAL_INDEX_TYPE))) {
				currentSpatialIndexType = null;
				break;
			}
		}
		// 不先置为-1会导致不能正确触发事件
		comboBoxIndexType.setSelectedIndex(-1);
		comboBoxIndexType.setSelectedItem(currentSpatialIndexType);

	}

	private void checkScrollPanelDescribe() {
		Object selectedItem = comboBoxIndexType.getSelectedItem();
		if (selectedItem == null) {
			scrollPaneDescribe.setViewportView(null);
		} else if (selectedItem.equals(SpatialIndexTypeUtilties.toString(SpatialIndexType.NONE))) {
			scrollPaneDescribe.setViewportView(textAreaNull);
		} else if (selectedItem.equals(SpatialIndexTypeUtilties.toString(SpatialIndexType.RTREE))) {
			scrollPaneDescribe.setViewportView(textAreaRTree);
		} else if (selectedItem.equals(SpatialIndexTypeUtilties.toString(SpatialIndexType.QTREE))) {
			scrollPaneDescribe.setViewportView(textAreaRTree);
		} else if (selectedItem.equals(SpatialIndexTypeUtilties.toString(SpatialIndexType.MULTI_LEVEL_GRID))) {
			scrollPaneDescribe.setViewportView(panelDynamicIndex);
			initPanelDynamic();
		} else if (selectedItem.equals(SpatialIndexTypeUtilties.toString(SpatialIndexType.TILE))) {
			scrollPaneDescribe.setViewportView(panelGraphIndex);
			initPanelGraph();
		}
	}

	/**
	 * 根据当前选中的值初始化动态索引面板
	 */
	private void initPanelDynamic() {
		java.util.List<SpatialIndexInfo> selectedSpatialIndexInfo = getSpatialIndexInfos();

		this.panelDynamicIndex.setX(SpatialIndexInfoUtilties.getSpatialIndexInfoX(selectedSpatialIndexInfo));
		this.panelDynamicIndex.setY(SpatialIndexInfoUtilties.getSpatialIndexInfoY(selectedSpatialIndexInfo));
		this.panelDynamicIndex.setGrid0(SpatialIndexInfoUtilties.getSpatialIndexInfoGrid0(selectedSpatialIndexInfo));
		this.panelDynamicIndex.setGrid1(SpatialIndexInfoUtilties.getSpatialIndexInfoGrid1(selectedSpatialIndexInfo));
		this.panelDynamicIndex.setGrid2(SpatialIndexInfoUtilties.getSpatialIndexInfoGrid2(selectedSpatialIndexInfo));
	}

	/**
	 * 根据当前选中的值初始化图库索引面板
	 */
	private void initPanelGraph() {
		java.util.List<SpatialIndexInfo> selectedSpatialIndexInfo = getSpatialIndexInfos();

		List<Dataset> selectedDatasets = new ArrayList<>();
		int[] selectedRows = tableDatasets.getSelectedRows();
		for (int selectedRow : selectedRows) {
			selectedDatasets.add((Dataset) spatialIndexTableModel.getValueAt(selectedRow, SpatialIndexTableModel.COLUMN_DATASET));
		}

		this.panelGraphIndex.setFieldModel(DatasetUtilties.getCommonFields(selectedDatasets));
		this.panelGraphIndex.setField(SpatialIndexInfoUtilties.getSpatialIndexInfoTileField(selectedSpatialIndexInfo));
		this.panelGraphIndex.setWidth(SpatialIndexInfoUtilties.getSpatialIndexInfoTileWidth(selectedSpatialIndexInfo));
		this.panelGraphIndex.setHeight(SpatialIndexInfoUtilties.getSpatialIndexInfoTileHeight(selectedSpatialIndexInfo));

	}

	private List<SpatialIndexInfo> getSpatialIndexInfos() {
		List<SpatialIndexInfo> selectedSpatialIndexInfo = new ArrayList<>();
		int[] selectedRows = tableDatasets.getSelectedRows();
		for (int selectedRow : selectedRows) {
			selectedSpatialIndexInfo.add(spatialIndexTableModel.getSpatialIndexInfo(selectedRow));
		}
		return selectedSpatialIndexInfo;
	}

	private void buttonOkClick() {
		if (spatialIndexTableModel.bulid()) {
			this.dispose();
		}
	}

	private void setTableValues(Object selectedItem) {
		int[] selectedRows = tableDatasets.getSelectedRows();
		for (int selectedRow : selectedRows) {
			tableDatasets.setValueAt(selectedItem, selectedRow, SpatialIndexTableModel.COLUMN_DEAL_INDEX_TYPE);
		}
	}
	//endregion


	private void initComponentStates() {
		this.buttonSelectAll.setEnabled(false);
		this.buttonSelectInvert.setEnabled(false);
		this.buttonDelete.setEnabled(false);
		this.buttonOk.setEnabled(false);

		java.util.List<Dataset> addDataset = new ArrayList<>();
		Dataset[] activeDatasets = Application.getActiveApplication().getActiveDatasets();
		for (Dataset activeDataset : activeDatasets) {
			if (SpatialIndexTableModelBean.isSupportDatasetType(activeDataset.getType())) {
				addDataset.add(activeDataset);
			}
		}
		if (activeDatasets != null && activeDatasets.length > 0) {
			spatialIndexTableModel.addDatasets(addDataset);
			tableDatasets.setRowSelectionInterval(0, 0);
		}

	}

	private void initResources() {
		this.setTitle(DataEditorProperties.getString("String_SpatialIndexManager"));
		this.textAreaNull.setText("    " + CoreProperties.getString("String_NoneSpatialIndexDescrition"));
		this.textAreaRTree.setText("    " + CoreProperties.getString("String_RtreeDescription"));
		this.textAreaQuadTree.setText("    " + CoreProperties.getString("String_QtreeDescription"));
		this.labelIndexType.setText(ControlsProperties.getString("String_LabelSpatialIndexType"));
		this.buttonOk.setText(CommonProperties.getString(CommonProperties.OK));
		this.buttonCancle.setText(CommonProperties.getString(CommonProperties.Cancel));
	}

	@Override
	public void windowClosing(WindowEvent e) {
		this.dispose();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	class SpatialIndexTypeCellEditor extends DefaultCellEditor {

		private JComboBox<String> comboBox;


		public SpatialIndexTypeCellEditor(JComboBox comboBox) {
			super(comboBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			getCombobox();
			this.comboBox.setModel(new DefaultComboBoxModel<String>(SpatialIndexTypeUtilties.getSupportSpatialIndexType((Dataset) table.getValueAt(row, SpatialIndexTableModel.COLUMN_DATASET))));
			this.comboBox.setSelectedItem(table.getValueAt(row, column));
			return this.comboBox;
		}

		private void getCombobox() {
			if (this.comboBox == null) {
				this.comboBox = new JComboBox<>();
				this.comboBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							fireEditingStopped();
						}
					}
				});
			}
		}

		@Override
		public Object getCellEditorValue() {
			return this.comboBox.getSelectedItem();
		}
	}
}
