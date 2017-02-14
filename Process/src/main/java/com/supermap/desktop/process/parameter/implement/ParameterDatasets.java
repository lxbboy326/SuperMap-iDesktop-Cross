package com.supermap.desktop.process.parameter.implement;

import com.supermap.desktop.process.enums.ParameterType;
import com.supermap.desktop.process.parameter.interfaces.IMultiSelectionParameter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author XiaJT
 */
public class ParameterDatasets extends AbstractParameter implements IMultiSelectionParameter {

	private JPanel panel;
	private List datasets = new ArrayList<>();

	@Override
	public String getType() {
		return ParameterType.DATASETS;
	}

	@Override
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
		}
		return panel;
	}


	@Override
	public Object getSelectedItem() {
		return new Object[0];
	}

	@Override
	public void setSelectedItem(Object selectedItems) {

	}

	@Override
	public void dispose() {

	}
}
