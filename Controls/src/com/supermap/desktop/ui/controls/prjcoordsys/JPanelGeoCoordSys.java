package com.supermap.desktop.ui.controls.prjcoordsys;

import com.supermap.data.Enum;
import com.supermap.data.GeoCoordSys;
import com.supermap.data.GeoCoordSysType;
import com.supermap.data.GeoDatumType;
import com.supermap.data.GeoPrimeMeridianType;
import com.supermap.data.GeoSpheroidType;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.ui.controls.TextFields.ISmTextFieldLegit;
import com.supermap.desktop.ui.controls.TextFields.SmTextFieldLegit;
import com.supermap.desktop.utilties.StringUtilties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author XiaJT
 */
public class JPanelGeoCoordSys extends JPanel {

	private JLabel labelType = new JLabel();
	private JComboBox<GeoCoordSysType> comboBoxType = new JComboBox<>();

	// 大地参考系
	private JPanel panelGeoDatum = new JPanel();
	private JLabel labelGeoDatumType = new JLabel();
	private JComboBox<GeoDatumType> comboBoxGeoDatumType = new JComboBox<>();

	// 椭球参数
	private JPanel panelGeoSpheroid = new JPanel();
	private JLabel labelGeoSpheroidType = new JLabel();
	private JComboBox<GeoSpheroidType> comboBoxGeoSpheroidType = new JComboBox<>();
	private JLabel labelAxis = new JLabel();
	private SmTextFieldLegit textFieldAxis = new SmTextFieldLegit();
	private JLabel labelFlatten = new JLabel();
	private SmTextFieldLegit textFieldFlatten = new SmTextFieldLegit();

	// 中央经线
	private JPanel panelCentralMeridian = new JPanel();
	private JLabel labelCentralMeridianType = new JLabel();
	private JComboBox<GeoPrimeMeridianType> comboBoxCentralMeridianType = new JComboBox<>();
	private JLabel labelLongitude = new JLabel();
	private SmTextFieldLegit textFieldLongitude = new SmTextFieldLegit();

	private GeoCoordSys geoCoordSys = new GeoCoordSys();
	// 加锁防止事件循环触发
	private boolean lock = false;
	private boolean lockGeo = false;
	private boolean lockAxis = false;
	private boolean lockCenter = false;

	public JPanelGeoCoordSys() {
		initComponents();
		addListeners();
		initLayout();
		initResources();
		initComponentStates();
	}

	private void initComponents() {
		// TODO: 2016/5/17 初始化
		//region 类型
		Enum[] enums = Enum.getEnums(GeoCoordSysType.class);

		for (Enum anEnum : enums) {
			if (anEnum instanceof GeoCoordSysType)
				comboBoxType.addItem((GeoCoordSysType) anEnum);
		}
		comboBoxType.setRenderer(new ListCellRenderer<GeoCoordSysType>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends GeoCoordSysType> list, GeoCoordSysType value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel jLabel = new JLabel();
				jLabel.setText(" " + value.name());
				if (isSelected) {
					jLabel.setOpaque(true);
					jLabel.setBackground(list.getSelectionBackground());
				}
				return jLabel;
			}
		});
		//endregion

		//region 大地参考系类型
		Enum[] enumsGeoDatum = Enum.getEnums(GeoDatumType.class);
		for (Enum anEnum : enumsGeoDatum) {
			if (anEnum instanceof GeoDatumType) {
				comboBoxGeoDatumType.addItem((GeoDatumType) anEnum);
			}
		}
		comboBoxGeoDatumType.setRenderer(new ListCellRenderer<GeoDatumType>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends GeoDatumType> list, GeoDatumType value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel jLabel = new JLabel();
				jLabel.setText(" " + value.name());
				if (isSelected) {
					jLabel.setOpaque(true);
					jLabel.setBackground(list.getSelectionBackground());
				}
				return jLabel;
			}
		});
		//endregion

		//region 椭球参数类型
		Enum[] enumsGeoSpheroid = Enum.getEnums(GeoSpheroidType.class);
		for (Enum anEnum : enumsGeoSpheroid) {
			if (anEnum instanceof GeoSpheroidType) {
				comboBoxGeoSpheroidType.addItem((GeoSpheroidType) anEnum);
			}
		}
		comboBoxGeoSpheroidType.setRenderer(new ListCellRenderer<GeoSpheroidType>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends GeoSpheroidType> list, GeoSpheroidType value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel jLabel = new JLabel();
				jLabel.setText(" " + value.name());
				if (isSelected) {
					jLabel.setOpaque(true);
					jLabel.setBackground(list.getSelectionBackground());
				}
				return jLabel;
			}
		});
		//endregion

		//region 赤道半径
		textFieldAxis.setSmTextFieldLegit(new ISmTextFieldLegit() {
			@Override
			public boolean isTextFieldValueLegit(String textFieldValue) {
				if (StringUtilties.isNullOrEmpty(textFieldValue) || textFieldValue.contains("d")) {
					return false;
				}
				try {
					double value = Double.valueOf(textFieldValue);
					return axisValueChanged(value);
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public String getLegitValue(String currentValue, String backUpValue) {
				return backUpValue;
			}
		});
		//endregion

		//region 扁率
		textFieldFlatten.setSmTextFieldLegit(new ISmTextFieldLegit() {
			@Override
			public boolean isTextFieldValueLegit(String textFieldValue) {
				if (StringUtilties.isNullOrEmpty(textFieldValue) || textFieldValue.contains("d")) {
					return false;
				}
				try {
					double value = Double.valueOf(textFieldValue);
					return flattenValueChanged(value);
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public String getLegitValue(String currentValue, String backUpValue) {
				return backUpValue;
			}
		});
		//endregion
		//region 中央经线
		Enum[] enumsCenter = Enum.getEnums(GeoPrimeMeridianType.class);

		for (Enum anEnum : enumsCenter) {
			if (anEnum instanceof GeoPrimeMeridianType)
				comboBoxCentralMeridianType.addItem((GeoPrimeMeridianType) anEnum);
		}
		comboBoxCentralMeridianType.setRenderer(new ListCellRenderer<GeoPrimeMeridianType>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends GeoPrimeMeridianType> list, GeoPrimeMeridianType value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel jLabel = new JLabel();
				jLabel.setText(" " + value.name());
				if (isSelected) {
					jLabel.setOpaque(true);
					jLabel.setBackground(list.getSelectionBackground());
				}
				return jLabel;
			}
		});
		//endregion
		//region 经度
		textFieldLongitude.setSmTextFieldLegit(new ISmTextFieldLegit() {
			@Override
			public boolean isTextFieldValueLegit(String textFieldValue) {
				if (StringUtilties.isNullOrEmpty(textFieldValue) || textFieldValue.contains("d")) {
					return false;
				}
				try {
					double value = Double.valueOf(textFieldValue);
					return longitudeValueChanged(value);
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public String getLegitValue(String currentValue, String backUpValue) {
				return backUpValue;
			}
		});
		//endregion
	}

	private boolean longitudeValueChanged(double value) {
		if (value < -180 || value > 180) {
			return false;
		}
		if (!textFieldLongitude.getText().equals(textFieldLongitude.getBackUpValue())) {
			if (!lockCenter) {
				comboBoxCentralMeridianType.setSelectedItem(GeoPrimeMeridianType.PRIMEMERIDIAN_USER_DEFINED);
			}
			if (geoCoordSys.getGeoPrimeMeridian().getType() == GeoPrimeMeridianType.PRIMEMERIDIAN_USER_DEFINED) {
				geoCoordSys.getGeoPrimeMeridian().setLongitudeValue(value);
			}
		}
		return true;
	}

	private boolean flattenValueChanged(double value) {
		if (value < 0 || value > 1) {
			return false;
		}
		if (!textFieldFlatten.getText().equals(textFieldFlatten.getBackUpValue())) {
			if (!lockAxis) {
				comboBoxGeoSpheroidType.setSelectedItem(GeoSpheroidType.SPHEROID_USER_DEFINED);
			}
			if (geoCoordSys.getGeoDatum().getGeoSpheroid().getType() == GeoSpheroidType.SPHEROID_USER_DEFINED) {
				geoCoordSys.getGeoDatum().getGeoSpheroid().setFlatten(value);
			}
		}
		return true;
	}

	private boolean axisValueChanged(double value) {
		if (value < 5000000 || value > 10000000) {
			return false;
		}
		if (!textFieldAxis.getText().equals(textFieldAxis.getBackUpValue())) {
			if (!lockAxis) {
				comboBoxGeoSpheroidType.setSelectedItem(GeoSpheroidType.SPHEROID_USER_DEFINED);
			}
			if (geoCoordSys.getGeoDatum().getGeoSpheroid().getType() == GeoSpheroidType.SPHEROID_USER_DEFINED) {
				geoCoordSys.getGeoDatum().getGeoSpheroid().setAxis(value);
			}
		}
		return true;
	}

	private void addListeners() {
		comboBoxType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					geoCoordSys.setType((GeoCoordSysType) comboBoxType.getSelectedItem());
					if (comboBoxType.getSelectedItem() != GeoCoordSysType.GCS_USER_DEFINE) {
						lock = true;
						comboBoxGeoDatumType.setSelectedItem(geoCoordSys.getGeoDatum().getType());
						comboBoxCentralMeridianType.setSelectedItem(geoCoordSys.getGeoPrimeMeridian().getType());
						lock = false;
					}
					firePropertyChange("GeoCoordSysType", "", "");
				}
			}
		});
		comboBoxGeoDatumType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					geoCoordSys.getGeoDatum().setType((GeoDatumType) comboBoxGeoDatumType.getSelectedItem());
					if (!lock) {
						comboBoxType.setSelectedItem(GeoCoordSysType.GCS_USER_DEFINE);
					}
					if (comboBoxGeoDatumType.getSelectedItem() != GeoDatumType.DATUM_USER_DEFINED) {
						lockGeo = true;
						comboBoxGeoSpheroidType.setSelectedItem(geoCoordSys.getGeoDatum().getGeoSpheroid().getType());
						lockGeo = false;
					}
				}
			}
		});

		comboBoxGeoSpheroidType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					geoCoordSys.getGeoDatum().getGeoSpheroid().setType((GeoSpheroidType) comboBoxGeoSpheroidType.getSelectedItem());
					if (!lockGeo) {
						comboBoxGeoDatumType.setSelectedItem(GeoDatumType.DATUM_USER_DEFINED);
					}
					if (comboBoxGeoSpheroidType.getSelectedItem() != GeoSpheroidType.SPHEROID_USER_DEFINED) {
						lockAxis = true;
						textFieldAxis.setText(String.valueOf(geoCoordSys.getGeoDatum().getGeoSpheroid().getAxis()));
						textFieldFlatten.setText(String.valueOf(geoCoordSys.getGeoDatum().getGeoSpheroid().getFlatten()));
						lockAxis = false;
					}
				}
			}
		});

		comboBoxCentralMeridianType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					geoCoordSys.getGeoPrimeMeridian().setType((GeoPrimeMeridianType) comboBoxCentralMeridianType.getSelectedItem());
					if (!lock) {
						comboBoxType.setSelectedItem(GeoCoordSysType.GCS_USER_DEFINE);
					}
					if (comboBoxCentralMeridianType.getSelectedItem() != GeoPrimeMeridianType.PRIMEMERIDIAN_USER_DEFINED) {
						lockCenter = true;
						textFieldLongitude.setText(String.valueOf(geoCoordSys.getGeoPrimeMeridian().getLongitudeValue()));
						lockCenter = false;
					}
				}
			}
		});
	}

	//region 初始化布局
	private void initLayout() {
		initPanelGeoDatum();
		initPanelCentralMeridian();
		this.setLayout(new GridBagLayout());
		this.add(labelType, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(0, 0).setFill(GridBagConstraints.NONE).setIpad(50, 0).setInsets(10, 10, 0, 0));
		this.add(comboBoxType, new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(2, 0).setFill(GridBagConstraints.HORIZONTAL).setInsets(10, 5, 0, 20));
		this.add(panelGeoDatum, new GridBagConstraintsHelper(0, 1, 2, 1).setWeight(2, 0).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setInsets(5, 10, 0, 10));
		this.add(panelCentralMeridian, new GridBagConstraintsHelper(0, 2, 2, 1).setWeight(2, 0).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setInsets(5, 10, 10, 10));
	}


	private void initPanelGeoDatum() {
		// 大地参考系
		initPanelGeoSpheroid();
		panelGeoDatum.setBorder(BorderFactory.createTitledBorder(ControlsProperties.getString("String_GeoDatum")));
		panelGeoDatum.setLayout(new GridBagLayout());
		panelGeoDatum.add(labelGeoDatumType, new GridBagConstraintsHelper(0, 0, 1, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.WEST).setWeight(0, 1).setInsets(5, 5, 0, 0).setIpad(39, 0));
		panelGeoDatum.add(comboBoxGeoDatumType, new GridBagConstraintsHelper(1, 0, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setInsets(5, 5, 0, 5));
		panelGeoDatum.add(panelGeoSpheroid, new GridBagConstraintsHelper(0, 1, 2, 1).setFill(GridBagConstraints.BOTH).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setInsets(5, 0, 0, 0));
	}

	private void initPanelGeoSpheroid() {
		// 椭球参数
		panelGeoSpheroid.setBorder(BorderFactory.createTitledBorder(ControlsProperties.getString("String_GeoSpheroid")));
		panelGeoSpheroid.setLayout(new GridBagLayout());
		panelGeoSpheroid.add(labelGeoSpheroidType, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(0, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setIpad(32, 0).setInsets(5, 5, 0, 0));
		panelGeoSpheroid.add(comboBoxGeoSpheroidType, new GridBagConstraintsHelper(1, 0, 3, 1).setWeight(2, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 5, 0, 0));

		panelGeoSpheroid.add(labelAxis, new GridBagConstraintsHelper(0, 1, 1, 1).setWeight(0, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 5, 0, 0));
		panelGeoSpheroid.add(textFieldAxis, new GridBagConstraintsHelper(1, 1, 1, 1).setWeight(1, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 5, 0, 0));
		panelGeoSpheroid.add(labelFlatten, new GridBagConstraintsHelper(2, 1, 1, 1).setWeight(0, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 5, 0, 0).setIpad(30, 0));
		panelGeoSpheroid.add(textFieldFlatten, new GridBagConstraintsHelper(3, 1, 1, 1).setWeight(1, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 5, 0, 0));
	}

	private void initPanelCentralMeridian() {
		// 中央经线
		panelCentralMeridian.setBorder(BorderFactory.createTitledBorder(CoreProperties.getString("String_PrjParameter_CenterMeridian")));
		panelCentralMeridian.setLayout(new GridBagLayout());
		panelCentralMeridian.add(labelCentralMeridianType, new GridBagConstraintsHelper(0, 0, 1, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.WEST).setWeight(0, 1).setIpad(39, 0).setInsets(5, 5, 0, 0));
		panelCentralMeridian.add(comboBoxCentralMeridianType, new GridBagConstraintsHelper(1, 0, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setInsets(5, 5, 0, 5));

		panelCentralMeridian.add(labelLongitude, new GridBagConstraintsHelper(0, 1, 1, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.WEST).setWeight(0, 1).setInsets(5, 5, 0, 0));
		panelCentralMeridian.add(textFieldLongitude, new GridBagConstraintsHelper(1, 1, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setInsets(5, 5, 0, 5));
	}

	//endregion

	private void initResources() {
		labelType.setText(ControlsProperties.getString("String_Label_Type"));
		labelCentralMeridianType.setText(ControlsProperties.getString("String_Label_Type"));
		labelGeoDatumType.setText(ControlsProperties.getString("String_Label_Type"));
		labelGeoSpheroidType.setText(ControlsProperties.getString("String_Label_Type"));
		labelAxis.setText(ControlsProperties.getString("String_Axis"));
		labelFlatten.setText(ControlsProperties.getString("String_Flatten"));
		labelLongitude.setText(ControlsProperties.getString("String_Label_LongitudeValue"));
	}

	private void initComponentStates() {
		lock = true;
		lockGeo = true;
		lockAxis = true;
		lockCenter = true;
		comboBoxType.setSelectedItem(this.geoCoordSys.getType());
		if (this.getGeoCoordSys().getType() == GeoCoordSysType.GCS_USER_DEFINE) {
			comboBoxGeoDatumType.setSelectedItem(this.geoCoordSys.getGeoDatum().getType());
			if (this.geoCoordSys.getGeoDatum().getType() == GeoDatumType.DATUM_USER_DEFINED) {
				comboBoxGeoSpheroidType.setSelectedItem(this.geoCoordSys.getGeoDatum().getGeoSpheroid().getType());
				if (this.geoCoordSys.getGeoDatum().getGeoSpheroid().getType() == GeoSpheroidType.SPHEROID_USER_DEFINED) {
					textFieldAxis.setText(String.valueOf(this.geoCoordSys.getGeoDatum().getGeoSpheroid().getAxis()));
					textFieldFlatten.setText(String.valueOf(this.geoCoordSys.getGeoDatum().getGeoSpheroid().getFlatten()));
				}

				comboBoxCentralMeridianType.setSelectedItem(this.geoCoordSys.getGeoPrimeMeridian().getType());
				if (this.geoCoordSys.getGeoPrimeMeridian().getType() == GeoPrimeMeridianType.PRIMEMERIDIAN_USER_DEFINED) {
					textFieldLongitude.setText(String.valueOf(this.geoCoordSys.getGeoPrimeMeridian().getLongitudeValue()));
				}
			}
		}
		lock = false;
		lockGeo = false;
		lockAxis = false;
		lockCenter = false;
	}

	public GeoCoordSys getGeoCoordSys() {
		return geoCoordSys.clone();
	}

	public void setGeoCoordSys(GeoCoordSys geoCoordSys) {
		if (this.geoCoordSys != null) {
			this.geoCoordSys.dispose();
		}
		this.geoCoordSys = geoCoordSys.clone();
		initComponentStates();
	}
}