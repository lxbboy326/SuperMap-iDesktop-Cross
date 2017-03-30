package com.supermap.desktop.dialog;

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Rectangle2D;
import com.supermap.desktop.Application;
import com.supermap.desktop.controls.ControlDefaultValues;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.controls.utilities.ComponentUIUtilities;
import com.supermap.desktop.controls.utilities.ControlsResources;
import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.mapview.map.propertycontrols.PanelGroupBoxViewBounds;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.FileChooserControl;
import com.supermap.desktop.ui.controls.SmDialog;
import com.supermap.desktop.ui.controls.SmFileChoose;
import com.supermap.desktop.ui.controls.TextFields.WaringTextField;
import com.supermap.desktop.ui.controls.borderPanel.PanelButton;
import com.supermap.desktop.utilities.*;
import com.supermap.mapping.ImageType;
import com.supermap.mapping.Map;
import sun.swing.plaf.synth.SynthFileChooserUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.MessageFormat;

/**
 * @author YuanR
 *         地图输出为图片窗体
 */
public class DiglogMapOutputPicture extends SmDialog {

	private Map map;
	private JPanel mainPanel;
	private JPanel outputSetPanel;
	private PanelGroupBoxViewBounds panelGroupBoxViewBounds;
	private PanelButton panelButton;

	private JLabel fileNameLabel;
	// 带有路径选择按钮的textFile
	private FileChooserControl fileChooserControlExportPath;

	private JLabel resolutionLabel;
	// 带有警告提示的textFile
	private WaringTextField resolutionTextField;
	private JLabel DPILabel;

	private JLabel widthLabel;
	private JLabel heightLabel;
	private JTextField widthTextField;
	private JTextField heightTextField;

	private JLabel expectedMemoryLabel;
	private WaringTextField occupiedMemoryTextField;


	// 预计占用内存label，模块暂不实现
	private JCheckBox backTransparent;

	private SmFileChoose exportPathFileChoose;
	private WaringTextField waringTextFieldLeft;
	private WaringTextField waringTextFieldTop;
	private WaringTextField waringTextFieldRight;
	private WaringTextField waringTextFieldBottom;
	private MapOutputPictureProgressCallable mapOutputPictureProgressCallable;

	private WindowsFileChooserUI windowsFileChooserUI;
	private SynthFileChooserUI synthFileChooserUI;

	private static final int DEFAULT_LABELSIZE = 80;
	private static final int DEFAULT_GAP = 16;
	private static final int DPI_START = 1;
	private static final int DPI_END = 65535;

	// 导出所需参数
	private String path = "";
	private int dpi = 0;
	private ImageType imageType = null;
	private Rectangle2D outPutBounds = null;
	private boolean isBackTransparent = false;
	//其他参数
	private double remainingMemory = 0.0;
	private double expectedMemory = 0.0;
	// 其他参数
	private double width = 0.0;
	private double height = 0.0;
	private String fileName = "";


	/**
	 * 默认构造方法
	 */
	public DiglogMapOutputPicture() {
		super();
		initComponents();
		setComponentName();
		initResources();
		initLayout();
		// 初始化各个控件的默认设置
		registEvents();
		// 初始化各个控件的默认设置
		initParameter();
		this.setResizable(true);
		this.pack();
		this.setSize(new Dimension(680, this.getPreferredSize().height));
		this.setLocationRelativeTo(null);
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponents() {
		this.map = MapUtilities.getMapControl().getMap();

		this.mainPanel = new JPanel();
		this.outputSetPanel = new JPanel();
		this.panelGroupBoxViewBounds = new PanelGroupBoxViewBounds(this);

		this.waringTextFieldLeft = panelGroupBoxViewBounds.getTextFieldCurrentViewLeft();
		this.waringTextFieldTop = panelGroupBoxViewBounds.getTextFieldCurrentViewTop();
		this.waringTextFieldRight = panelGroupBoxViewBounds.getTextFieldCurrentViewRight();
		this.waringTextFieldBottom = panelGroupBoxViewBounds.getTextFieldCurrentViewBottom();

		this.panelButton = new PanelButton();
		// 实现ctrl+enter执行操作
		if (this.componentList.size() > 0) {
			this.componentList.clear();
		}
		this.componentList.add(panelButton.getButtonOk());
		this.componentList.add(panelButton.getButtonCancel());
		this.setFocusTraversalPolicy(policy);
		this.getRootPane().setDefaultButton(panelButton.getButtonOk());


		this.fileNameLabel = new JLabel("FileName");
		this.fileChooserControlExportPath = new FileChooserControl();
		this.resolutionLabel = new JLabel("resolution");
		this.resolutionTextField = new WaringTextField();
		// 设置分辨率文本框只能输入数字
		this.DPILabel = new JLabel("DPI");

		this.widthLabel = new JLabel("width");
		this.heightLabel = new JLabel("height");
		this.widthTextField = new JTextField();
		this.widthTextField.setEditable(false);
		this.widthTextField.setPreferredSize(ControlDefaultValues.DEFAULT_PREFERREDSIZE);
		this.heightTextField = new JTextField();
		this.heightTextField.setEditable(false);
		this.heightTextField.setPreferredSize(ControlDefaultValues.DEFAULT_PREFERREDSIZE);

		this.expectedMemoryLabel = new JLabel("expectedMemory");
		this.occupiedMemoryTextField = new WaringTextField();
		this.occupiedMemoryTextField.getTextField().setEditable(false);
		this.occupiedMemoryTextField.getTextField().setBorder(new EmptyBorder(0, 0, 0, 0));

		this.backTransparent = new JCheckBox("backTransparent");
	}

	private void setComponentName() {
		ComponentUIUtilities.setName(this.backTransparent, "DiglogMapOutputPicture_backTransparent");
		ComponentUIUtilities.setName(this.DPILabel, "DiglogMapOutputPicture_DPILabel");
	}

	/**
	 * 初始化界面布局
	 */
	private void initLayout() {
		intiOutputSetPanelLayout();
		initMainPanelLayout();

		this.setLayout(new BorderLayout());
		this.add(this.mainPanel, BorderLayout.CENTER);
		this.add(this.panelButton, BorderLayout.SOUTH);
	}

	/**
	 * 导出设置面板布局设计
	 */
	private void intiOutputSetPanelLayout() {

		this.outputSetPanel.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("String_ExportPropertySet")));
		GroupLayout outputSetPanelLayout = new GroupLayout(this.outputSetPanel);
		outputSetPanelLayout.setAutoCreateContainerGaps(true);
		outputSetPanelLayout.setAutoCreateGaps(true);
		this.outputSetPanel.setLayout(outputSetPanelLayout);

		// @formatter:off
		outputSetPanelLayout.setHorizontalGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(outputSetPanelLayout.createSequentialGroup()
						.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(this.fileNameLabel)
								.addComponent(this.resolutionLabel)
								.addComponent(this.widthLabel)
								.addComponent(this.heightLabel))
						.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(outputSetPanelLayout.createSequentialGroup().addGap(DEFAULT_GAP).addComponent(fileChooserControlExportPath,GroupLayout.PREFERRED_SIZE,DEFAULT_LABELSIZE, Short.MAX_VALUE))
								.addGroup(outputSetPanelLayout.createSequentialGroup()
										.addComponent(resolutionTextField,GroupLayout.PREFERRED_SIZE,DEFAULT_LABELSIZE, Short.MAX_VALUE).addGap(7)
										.addComponent(DPILabel))
								.addGroup(outputSetPanelLayout.createSequentialGroup().addGap(DEFAULT_GAP).addComponent(this.widthTextField,GroupLayout.PREFERRED_SIZE,DEFAULT_LABELSIZE, Short.MAX_VALUE ))
								.addGroup(outputSetPanelLayout.createSequentialGroup().addGap(DEFAULT_GAP).addComponent(this.heightTextField,GroupLayout.PREFERRED_SIZE,DEFAULT_LABELSIZE, Short.MAX_VALUE))))
				.addGroup(outputSetPanelLayout.createSequentialGroup()
						.addComponent(this.expectedMemoryLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.occupiedMemoryTextField,GroupLayout.PREFERRED_SIZE,DEFAULT_LABELSIZE, Short.MAX_VALUE))
				.addGroup(outputSetPanelLayout.createSequentialGroup()
						.addComponent(this.backTransparent)));

		outputSetPanelLayout.setVerticalGroup(outputSetPanelLayout.createSequentialGroup()
				.addGroup(outputSetPanelLayout.createSequentialGroup()
					.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.fileNameLabel)
						.addComponent(this.fileChooserControlExportPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.resolutionLabel)
						.addComponent(this.resolutionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.DPILabel))
					.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.widthLabel)
						.addComponent(this.widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.heightLabel)
						.addComponent(this.heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGroup(outputSetPanelLayout.createSequentialGroup()
						.addGroup(outputSetPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.expectedMemoryLabel)
						.addComponent(this.occupiedMemoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(outputSetPanelLayout.createSequentialGroup()
						.addComponent(this.backTransparent))
				.addGap(5,5,Short.MAX_VALUE)));
		// @formatter:on
	}

	/**
	 * 主面板布局设计
	 */
	private void initMainPanelLayout() {
		GroupLayout mainPanelLayout = new GroupLayout(this.mainPanel);
		mainPanelLayout.setAutoCreateContainerGaps(true);
		mainPanelLayout.setAutoCreateGaps(true);
		this.mainPanel.setLayout(mainPanelLayout);

		//@formatter:off
         mainPanelLayout.setHorizontalGroup(mainPanelLayout.createSequentialGroup()
                   .addComponent(this.outputSetPanel,0,150,Short.MAX_VALUE)
                   .addComponent(this.panelGroupBoxViewBounds,0,150,Short.MAX_VALUE));
         mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup()
                   .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	    				.addComponent(this.outputSetPanel,220,220,220)
	    				.addComponent(this.panelGroupBoxViewBounds,220,220,220)));
         //@formatter:on


	}

	/**
	 * 资源化
	 */
	private void initResources() {
		this.setTitle(MapViewProperties.getString("String_OutputPicture"));
		this.fileNameLabel.setText(MapViewProperties.getString("String_FileName"));
		this.resolutionLabel.setText(CommonProperties.getString("String_Resolution"));
		this.DPILabel.setText("DPI");
		this.widthLabel.setText(MapViewProperties.getString("String_FormSavePicture_Width"));
		this.heightLabel.setText(MapViewProperties.getString("String_FormSavePicture_Height"));
		this.expectedMemoryLabel.setText(MapViewProperties.getString("String_ExpectedOccupyMemory"));

		this.backTransparent.setText(MapViewProperties.getString("String_PNG_BackTransparent"));
	}

	/**
	 * 添加监听事件
	 */
	private void registEvents() {
		removeEvents();
		this.fileChooserControlExportPath.getButton().addActionListener(this.exportPathLitener);
		this.fileChooserControlExportPath.getEditor().addCaretListener(this.exportPathCareListener);

		this.resolutionTextField.registEvents();
		this.resolutionTextField.getTextField().addCaretListener(this.resolutionCareListener);

		this.panelButton.getButtonCancel().addActionListener(CancelActionListener);
		this.panelButton.getButtonOk().addActionListener(OKActionListener);

		this.waringTextFieldLeft.getTextField().addCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldTop.getTextField().addCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldRight.getTextField().addCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldBottom.getTextField().addCaretListener(rangeTextFiledCareListener);

	}

	/**
	 * 移除监听事件
	 */
	private void removeEvents() {
		this.fileChooserControlExportPath.getButton().removeActionListener(this.exportPathLitener);
		this.fileChooserControlExportPath.getEditor().removeCaretListener(this.exportPathCareListener);

		this.resolutionTextField.removeEvents();
		this.resolutionTextField.getTextField().removeCaretListener(this.resolutionCareListener);

		this.panelButton.getButtonCancel().removeActionListener(CancelActionListener);
		this.panelButton.getButtonOk().removeActionListener(OKActionListener);

		this.waringTextFieldLeft.getTextField().removeCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldTop.getTextField().removeCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldRight.getTextField().removeCaretListener(rangeTextFiledCareListener);
		this.waringTextFieldBottom.getTextField().removeCaretListener(rangeTextFiledCareListener);
	}


	/**
	 * 设置输出图片的格式
	 * 背景是否透明只在 png和jpg有效
	 * DPI参数的出图支持四种格式 ：BMP\JPG\PNG/TIFF
	 *
	 * @param str
	 */
	private ImageType getImageType(String str) {
		if (str.contains(".png")) {
			this.resolutionTextField.setEnable(true);
			this.backTransparent.setEnabled(true);
			return this.imageType.PNG;
		} else if (str.contains(".jpg")) {
			this.resolutionTextField.setEnable(true);
			this.backTransparent.setEnabled(false);
			return this.imageType.JPG;
		} else if (str.contains(".bmp")) {
			this.resolutionTextField.setEnable(true);
			this.backTransparent.setEnabled(false);
			return this.imageType.BMP;
		} else if (str.contains(".gif")) {
			// 当数据类型为gif时，此时分辨率属性不可用，设置分辨率为默认值
			this.resolutionTextField.setText("96");
			this.resolutionTextField.setEnable(false);
			this.backTransparent.setEnabled(true);
			return this.imageType.GIF;
		} else if (str.contains(".eps")) {
			// 当数据类型为eps时，此时分辨率属性不可用，设置分辨率为默认值
			this.resolutionTextField.setText("96");
			this.resolutionTextField.setEnable(false);
			this.backTransparent.setEnabled(false);
			return this.imageType.EPS;
		} else if (str.contains(".tif")) {
			this.resolutionTextField.setEnable(true);
			this.backTransparent.setEnabled(false);
			// 暂时不支持tif
			return this.imageType.TIFF;
		} else {
			return null;
		}
	}

	/**
	 * 输出路劲的文本框改变监听
	 */
	private CaretListener exportPathCareListener = new CaretListener() {
		@Override
		public void caretUpdate(CaretEvent e) {
			// 当手动修改路劲文本框的值时，赋予其内容于路径参数和输出图片类型参数
			path = fileChooserControlExportPath.getEditor().getText();
			if (!StringUtilities.isNullOrEmpty(fileChooserControlExportPath.getEditor().getText())) {
				// 从字符中尝试提取需要导出的图片类型
				if (path.length() > 4) {
					path = path.substring(path.length() - 4, path.length());
				}
				imageType = getImageType(path);
				path = fileChooserControlExportPath.getEditor().getText();
				// 当路劲文本框改变时，判断其路径是否合法，并且初始化磁盘剩余内存情况
				initRemainingMemory();
			} else {
				path = "";
				imageType = null;
			}

			// 当手动输入的路径名称合法时，设置文件名称
			if (!StringUtilities.isNullOrEmpty(path)) {
				if (SystemPropertyUtilities.isWindows()) {
					fileName = path.substring(path.lastIndexOf("\\") + 1);
				} else {
					fileName = path.substring(path.lastIndexOf("/") + 1);
				}
			} else {
				// 当文件路径不合法时，也无法获得文件名
				fileName = "";
			}
			// 当路劲文本框改变时，判断一下确定按钮是否可用
			judgeOKButtonisEnabled();
		}
	};

	/**
	 * 分辨率textFiled，内容改变监听，主要用于：高、宽值的联动
	 */
	private CaretListener resolutionCareListener = new CaretListener() {
		@Override
		public void caretUpdate(CaretEvent e) {
			// 当textFiled为空时不做任何联动效果
			String str = resolutionTextField.getTextField().getText();
			// 不为空，并且是整型
			dpi = 0;
			if (!StringUtilities.isNullOrEmpty(str) && StringUtilities.isInteger(resolutionTextField.getTextField().getText())) {
				double textValue = StringUtilities.getNumber(resolutionTextField.getTextField().getText());
				if (textValue < Integer.MAX_VALUE) {
					dpi = Integer.parseInt(resolutionTextField.getTextField().getText());
					// 获得dpi的值并且值在值域范围内
					if (DPI_START > dpi && DPI_END < dpi) {
						dpi = 0;
					}
				}
			}
			setWidthANDHeight();
			// 当DPI文本框改变时，判断一下确定按钮是否可用
			judgeOKButtonisEnabled();
		}
	};


	/**
	 * 设置图片的宽度和高度，跟随分辨率和范围的变化而变化
	 */
	private void setWidthANDHeight() {
		// 图片的高度和宽度受分辨率和输出地图范围影响，当两者的值符合时，设置其宽度和高度
		double dFactor = 0.0;
		// 高度和宽度受DPI和范围矩形控制，英雌当要改变其高宽值时，需要判断DPI和范围矩形框是否存在
		if ((DPI_START <= dpi && dpi <= DPI_END) && outPutBounds != null) {
			if (map != null) {
				if (map.getPrjCoordSys().getType().equals(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE)) {
					dFactor = this.map.getScale() * 10.0 * 1000000000.0;
					this.width = ((outPutBounds.getWidth()) * dpi / 2540.0) * dFactor * 1.11355;
					this.height = ((outPutBounds.getHeight()) * dpi / 2540.0) * dFactor * 1.113;
				} else {
					dFactor = map.getScale() * 10.0 * 10000.0;
					this.width = ((outPutBounds.getWidth()) * dpi / 2540.0) * dFactor;
					this.height = ((outPutBounds.getHeight()) * dpi / 2540.0) * dFactor;
				}
				this.widthTextField.setText(DoubleUtilities.getFormatString(Math.round(width)));
				this.heightTextField.setText(DoubleUtilities.getFormatString(Math.round(height)));
			}
		} else {
			this.widthTextField.setText("0");
			this.heightTextField.setText("0");
		}
		// 内存消耗和高宽有关，当设置完高宽时，设置一下内存消耗
		setExpectedMemory(width, height);
	}

	/**
	 * 设置消耗内存的值，跟随分辨率的变化而变化
	 */
	private void setExpectedMemory(double width, double height) {
		double result = 0.0;
		try {
			double size = (width * height * 32) / 8 / 1024;//kb
			result = size / 1024 / 1024;//GB
			if (size / 1024 < 1) {
				this.occupiedMemoryTextField.setText(DoubleUtilities.getFormatString(Math.round(size)) + " KB");
			} else if (size / 1024 / 1024 < 1) {
				this.occupiedMemoryTextField.setText(DoubleUtilities.getFormatString(Math.round(size / 1024)) + " MB");
			} else {
				this.occupiedMemoryTextField.setText(DoubleUtilities.getFormatString(Math.round(size / 1024 / 1024)) + " GB");
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		this.expectedMemory = result;
		// 手动判断是否超出范围，当超出范围，设置警告图标显示，并设置提示信息
		if (this.expectedMemory >= remainingMemory) {
			DecimalFormat df = new DecimalFormat("0");
			this.occupiedMemoryTextField.getLabelWarning().setToolTipText(MessageFormat.format(MapViewProperties.getString("String_Insufficient_Disk_Space"), df.format(this.remainingMemory)));
			this.occupiedMemoryTextField.getLabelWarning().setIcon(ControlsResources.getIcon("/controlsresources/SnapSetting/warning.png"));
		} else {
			this.occupiedMemoryTextField.getLabelWarning().setIcon(null);
			this.panelButton.getButtonOk().setEnabled(true);
		}
		// 当内存超出范围时，置灰确定按钮
		judgeOKButtonisEnabled();
	}


	/**
	 * 当范围textFiled，内容改变监听
	 */
	private CaretListener rangeTextFiledCareListener = new CaretListener() {
		@Override
		public void caretUpdate(CaretEvent e) {
			// 判断文本框中输入的内容是否为纯数字
			// 当文本框中内容存在千分位时，做一下处理
			String leftText = waringTextFieldLeft.getTextField().getText().replace(",", "");
			String bottomText = waringTextFieldBottom.getTextField().getText().replace(",", "");
			String rightText = waringTextFieldRight.getTextField().getText().replace(",", "");
			String topText = waringTextFieldTop.getTextField().getText().replace(",", "");
			if (StringUtilities.isNumber(leftText) && StringUtilities.isNumber(bottomText)
					&& StringUtilities.isNumber(rightText) && StringUtilities.isNumber(topText)) {
				Rectangle2D rectangle2D = panelGroupBoxViewBounds.getRangeBound();
				if (rectangle2D != null) {
					outPutBounds = rectangle2D;
				} else {
					// 当无法获得有效范围矩形框时，设置导出范围矩形框为null
					outPutBounds = null;
				}
			} else {
				outPutBounds = null;
			}
			// 当范围文本框改变时，设置一下高度和宽度
			setWidthANDHeight();
			// 当范围文本框改变时，判断一下确定按钮是否可用
			judgeOKButtonisEnabled();
		}
	};

	/**
	 * 初始化参数，刷新面板
	 */
	private void initParameter() {
		// 初始化文件路径
		initFileChoose();
		// 初始化分辨率
		initResolution();
		// 初始化范围矩形框
		this.outPutBounds = panelGroupBoxViewBounds.getRangeBound();
		// 初始化宽度、高度
		setWidthANDHeight();
		judgeOKButtonisEnabled();
	}

	/**
	 * 初始化文件选择器、文件路径Filed默认路径
	 */
	private void initFileChoose() {
		String moduleName = "MapOutputPicture";
		if (!SmFileChoose.isModuleExist(moduleName)) {
			// 文件过滤器
			String fileFilters = SmFileChoose.bulidFileFilters(
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_PNG_FileFilter"),
							ControlsProperties.getString("String_PNG_Filters")),
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_JPG_FileFilter"),
							ControlsProperties.getString("String_JPG_Filters")),
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_Graphic_FileFilter"),
							ControlsProperties.getString("String_BMP_Filters")),
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_GIF_FileFilter"),
							ControlsProperties.getString("String_GIF_Filters")),
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_EPS_FileFilter"),
							ControlsProperties.getString("String_EPS_Filters")),
					SmFileChoose.createFileFilter(ControlsProperties.getString("String_TIFF_FileFilter"),
							ControlsProperties.getString("String_TIFF_Filters")));
//			CommonProperties.getString("String_DefaultFilePath")
			// windows和linux系统通用根目录
//			System.getProperty("user.dir")
			SmFileChoose.addNewNode(fileFilters, System.getProperty("user.dir"),
					ControlsProperties.getString("String_Save"), moduleName, "SaveOne");
		}
		this.exportPathFileChoose = new SmFileChoose(moduleName);
		// 分别获得各个系统下的FileChooserUI
		// 使文件选择器对话框更为智能
		// 利用反射机制，针对不同的操作系统，获得FileChooserUI，通过getFileName（）、setFileName（）两个方法实现：切换文件类型，文件名称跟随切换改变
		this.exportPathFileChoose.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (imageType != null) {

					FileChooserUI ui = exportPathFileChoose.getUI();
					String tempFileName = "";
					try {
						// 尝试获取子类中是否有getFileName（）方法
						Method getFileName = ui.getClass().getDeclaredMethod("getFileName");
						if (getFileName != null) {
							tempFileName = (String) getFileName.invoke(ui);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!StringUtilities.isNullOrEmpty(tempFileName)) {
						tempFileName = tempFileName.substring(0, tempFileName.length() - 4);
						// 获得文件类型的描述
						String tempFileType = exportPathFileChoose.getFileFilter().getDescription();
						if (tempFileType.indexOf(".png") > 0) {
							tempFileName = tempFileName + ".png";
						} else if (tempFileType.indexOf(".jpg") > 0) {
							tempFileName = tempFileName + ".jpg";
						} else if (tempFileType.indexOf(".bmp") > 0) {
							tempFileName = tempFileName + ".bmp";
						} else if (tempFileType.indexOf(".gif") > 0) {
							tempFileName = tempFileName + ".gif";
						} else if (tempFileType.indexOf(".eps") > 0) {
							tempFileName = tempFileName + ".eps";
						} else if (tempFileType.indexOf(".tif") > 0) {
							tempFileName = tempFileName + ".tif";
						}
						try {
							// 尝试获取子类中是否有setFileName（）方法
							Method setFileName = ui.getClass().getMethod("setFileName", String.class);
							if (setFileName != null) {
								setFileName.invoke(ui, tempFileName);
								fileName = tempFileName;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		// 两个系统下的获得最近路径得到的结果不同，windows得到的是路径，而linux得到的是完整的文件路径
		if (SystemPropertyUtilities.isWindows()) {
			// 对文件名进行判断，当目录下存在该文件时，名称重新给予
			int num = 1;
			String lastPath = this.exportPathFileChoose.getModuleLastPath() + "\\" + "ExportImage.png";
			File file = new File(lastPath);
			this.fileName = "ExportImage.png";
			while (file.exists()) {
				lastPath = this.exportPathFileChoose.getModuleLastPath() + "\\" + "ExportImage" + "_" + num + ".png";
				this.fileName = "ExportImage" + "_" + num + ".png";
				file = new File(lastPath);
				num++;
			}
			this.fileChooserControlExportPath.setText(lastPath);
		} else {
			String lastPath = this.exportPathFileChoose.getModuleLastPath();

			String filePath = lastPath.substring(0, lastPath.lastIndexOf("/") + 1);
			String fileType = lastPath.substring(lastPath.lastIndexOf("."));
			String fileName = lastPath.substring(lastPath.lastIndexOf("/") + 1, lastPath.lastIndexOf("."));

			int num = 1;
			File file = new File(lastPath);
			this.fileName = fileName;
			while (file.exists()) {
				lastPath = filePath + fileName + "_" + num + fileType;
				this.fileName = fileName + "_" + num + fileType;
				file = new File(lastPath);
				num++;
			}
			this.fileChooserControlExportPath.setText(lastPath);
		}
	}


	/**
	 * 路径设置按钮监听事件
	 */
	private ActionListener exportPathLitener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// 这里对文件名进行校正，当文件类型为空，并且文件名中含有小数点，取小数点前字符为文件名（文件名中不应该有小数点）
				if (imageType == null && fileName.indexOf(".") > 0) {
					fileName = fileName.substring(0, fileName.indexOf("."));
				}
				// 设置文件选择器选择的文件
				exportPathFileChoose.setSelectedFile(new File(fileName));
				// 当数据类型不为空时，打开文件选择对话框时，设置筛选器类型为当前数据类型
				if (imageType != null) {
					String imageTypeName = imageType.name();
					imageTypeName = imageTypeName.toLowerCase();
					// tiff文件名称预处理一下
					if (imageTypeName.equals("tiff")) {
						imageTypeName = "tif";
					}
					// 获得所有文件筛选器
					FileFilter[] fileFilter = exportPathFileChoose.getChoosableFileFilters();
					for (int i = 0; i < fileFilter.length; i++) {
						if ((fileFilter[i].getDescription()).contains(imageTypeName)) {
							// 设置初始筛选器类型为文件类型
							exportPathFileChoose.setFileFilter(fileFilter[i]);
						}
					}
				}

				int state = exportPathFileChoose.showSaveDialog(null);
				if (state == JFileChooser.APPROVE_OPTION) {
					// 设置输出图片的路径
					path = exportPathFileChoose.getFilePath();
					// 设置输出图片的格式
					getImageType(exportPathFileChoose.getFileFilter().getDescription());
					// 将路径赋予文本框
					fileChooserControlExportPath.getEditor().setText(path);
				}
			} catch (Exception ex) {
				Application.getActiveApplication().getOutput().output(ex);
			} finally {
				CursorUtilities.setDefaultCursor();
			}
		}
	};


	/**
	 * 初始化分辨率
	 */
	private void initResolution() {
		// 给其初始的分辨率值
		this.resolutionTextField.setText("96");
		this.dpi = 96;
		// 分辨率的范围为：(1,65535)
		this.resolutionTextField.setInitInfo(DPI_START, DPI_END, WaringTextField.POSITIVE_INTEGER_TYPE, "null");
	}

	/**
	 * 初始化磁盘剩余内存
	 * 当仅当路径改变时进行此方法
	 */
	private void initRemainingMemory() {
		// 设置内存文本框相关参数，因为内存是不断变化的，所以需要动态设置
		//1、判断文件路径是否正确，获得所存文件的根目录，以获取磁盘剩余信息
		String filePath = this.fileChooserControlExportPath.getEditor().getText();

		if (SystemPropertyUtilities.isWindows()) {
			if (!StringUtilities.isNullOrEmpty(filePath) && filePath.indexOf("\\") > 0) {
				//获得文件所在的文件夹目录
				filePath = filePath.substring(0, filePath.lastIndexOf('\\'));
				File file = new File(filePath);
				if (file.exists()) {
					double constm = 1024 * 1024 * 1024;
					this.remainingMemory = file.getFreeSpace() / constm;
					// 当磁盘剩余内存正确改变时，设置其内存限制范围
					setExpectedMemory(width, height);
				} else {
					// 如果此文件不存在，其路径错误，设置其路径为空，相应的图片类型为空
					this.path = "";
					this.remainingMemory = 0.0;
				}
			} else {
				this.path = "";
				this.remainingMemory = 0.0;
			}
		} else {
			if (!StringUtilities.isNullOrEmpty(filePath)) {
				//获得文件所在的文件夹目录
				filePath = filePath.substring(0, filePath.lastIndexOf("/"));
				File file = new File(filePath);
				if (file.exists()) {
					double constm = 1024 * 1024 * 1024;
					this.remainingMemory = file.getFreeSpace() / constm;
					setExpectedMemory(width, height);
				} else {
					// 如果此文件不存在，其路径错误，设置其路径为空，相应的图片类型为空
					this.path = "";
					this.remainingMemory = 0.0;
				}
			}
		}
	}

	/**
	 * 判断确定按钮是否可用
	 */
	private void judgeOKButtonisEnabled() {
		Boolean pathisValid = false;
		Boolean DPIisValid = false;
//		Boolean imageTypeisValid = false;
		Boolean outPutBoundsisValid = false;
		Boolean memory = false;

		if (!StringUtilities.isNullOrEmpty(path)) {
			pathisValid = true;
		}
		if (DPI_START <= dpi && dpi <= DPI_END) {
			DPIisValid = true;
		}
//		if (imageType != null) {
//			imageTypeisValid = true;
//		}
		if (outPutBounds != null) {
			outPutBoundsisValid = true;
			// 当矩形框范围错误时不允许复制其值
			this.panelGroupBoxViewBounds.getCopyButton().setEnabled(true);
		} else {
			this.panelGroupBoxViewBounds.getCopyButton().setEnabled(false);
		}
		if (expectedMemory < remainingMemory) {
			memory = true;
		}
		// 根据参数情况设置确定按钮是否可用
		if (pathisValid && DPIisValid && outPutBoundsisValid && memory) {
			this.panelButton.getButtonOk().setEnabled(true);
		} else {
			this.panelButton.getButtonOk().setEnabled(false);
		}
	}


	/**
	 * Cancel按钮点击监听
	 */
	private ActionListener CancelActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//注销事件
			removeEvents();
			//关闭窗口
			DiglogMapOutputPicture.this.dispose();
		}
	};

	/**
	 * OK按钮点击监听
	 */
	private ActionListener OKActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				boolean isSuccess = false;
				// 获得是否背景透明化参数信息
				isBackTransparent = backTransparent.isSelected();
				//设置光标为等待
				CursorUtilities.setWaitCursor(panelButton.getButtonOk());
				CursorUtilities.setWaitCursor(mainPanel);
				String resultMessage;
				// 将地图输出为图片之前，新建一个地图，对当前地图不会产生影响
				Map copyMap = new Map();
				copyMap = map;
				Application.getActiveApplication().getOutput().output(MapViewProperties.getString("String_OutputImage_AvoidAndFlowEnabled"));
				//  设置是否在出图的时候关掉地图的自动避让效果。
				copyMap.setDisableAutoAvoidEffect(true);
				// 设置是否在出图的时候关闭地图的动态效果
				copyMap.setDisableDynamicEffect(true);

				if (imageType.equals(imageType.GIF)) {
					if (copyMap.outputMapToGIF(path, isBackTransparent)) {
						isSuccess = true;
					}
				} else if (imageType.equals(imageType.EPS)) {
					if (copyMap.outputMapToEPS(path)) {
						isSuccess = true;
					}
				} else if (imageType.equals(imageType.EMF)) {
					if (copyMap.outputMapToEMF(path)) {
						isSuccess = true;
					}
				} else if (imageType.equals(imageType.BMP)) {
					if (copyMap.outputMapToBMP(path)) {
						isSuccess = true;
					}
				} else {
					if (copyMap.outputMapToFile(path, imageType, dpi, outPutBounds, isBackTransparent)) {
						isSuccess = true;
					}
				}
				// 输出结果信息
				if (isSuccess) {
					resultMessage = String.format(MapViewProperties.getString("String_OutputToFile_Successed"), path);
				} else {
					resultMessage = String.format(MapViewProperties.getString("String_OutputToFile_Failed"), path);
				}
				Application.getActiveApplication().getOutput().output(resultMessage);

			} catch (Exception e1) {
				Application.getActiveApplication().getOutput().output(e1);
			} finally {
				//注销事件
				removeEvents();
				//设置光标为正常
				CursorUtilities.setDefaultCursor(panelButton.getButtonOk());
				CursorUtilities.setDefaultCursor(mainPanel);
				//关闭窗口
				DiglogMapOutputPicture.this.dispose();
			}

			// 进度条实现
//			isBackTransparent = backTransparent.isSelected();
//			try {
//				FormProgress formProgress = new FormProgress();
//				mapOutputPictureProgressCallable = new MapOutputPictureProgressCallable(map, path, imageType, dpi, outPutBounds, isBackTransparent);
//				if (formProgress != null) {
//					formProgress.doWork(mapOutputPictureProgressCallable);
//				}
//			} catch (Exception e1) {
//				Application.getActiveApplication().getOutput().output(e1);
//			} finally {
//				removeEvents();
//				DiglogMapOutputPicture.this.dispose();
//			}
		}
	};
}
