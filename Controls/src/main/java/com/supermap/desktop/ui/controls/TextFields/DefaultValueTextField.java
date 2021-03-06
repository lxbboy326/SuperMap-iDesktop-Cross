package com.supermap.desktop.ui.controls.TextFields;

import com.supermap.desktop.utilities.StringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 获得带“提示信息”的文本框控件
 *
 * @author YuanR
 *         2017/2/16
 *         对控件进行重构，使其类似于“QQ”登录界面输入账号和密码的文本框--yuanR 2017.2.28
 */

public class DefaultValueTextField extends JTextField {

	private String defaultWarningValue;

	@Override
	public String getText() {
		if (!StringUtilities.isNullOrEmpty(this.defaultWarningValue)) {
			if (super.getText().equals(this.defaultWarningValue)) {
				return null;
			} else {
				return super.getText();
			}
		} else {
			return super.getText();
		}
	}


	/**
	 * 获得默认提示文本
	 *
	 * @return
	 */
	public String getDefaulWarningText() {
		return this.defaultWarningValue;
	}

	/**
	 * 默认构造方法
	 */
	public DefaultValueTextField() {
		super();
		initListener();
	}

	/**
	 * 输入默认提示信息
	 *
	 * @param defaultWarningValue
	 */
	public DefaultValueTextField(String defaultWarningValue) {
		this("", defaultWarningValue);
	}


	/**
	 * 构造函数
	 * 输入默认值和默认提示信息
	 *
	 * @param Value
	 * @param defaultWarningValue
	 */
	public DefaultValueTextField(String Value, String defaultWarningValue) {
		super(Value);
		setDefaulWarningText(defaultWarningValue);
		initListener();
	}


	/**
	 * 设置提示文本，当文本框值为空的时候，显示提示信息
	 *
	 * @param text
	 */
	public void setDefaulWarningText(String text) {
		if (!StringUtilities.isNullOrEmpty(text)) {
			this.defaultWarningValue = text;
			if (StringUtilities.isNullOrEmpty(getText())) {
				this.setText(this.defaultWarningValue);
				setForeground(Color.gray);
			}
		}
	}

	/**
	 * 添加监听
	 */
	private void initListener() {
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (getText() == null) {
					setText("");
					setForeground(Color.black);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (StringUtilities.isNullOrEmptyString(getText())) {
					setText(defaultWarningValue);
					setForeground(Color.gray);
				}
			}
		});
	}

	/**
	 * 清除输入的值，但不清楚默认提示信息
	 */
	public void clear() {
		this.setText(defaultWarningValue);
		this.setForeground(Color.gray);
	}

	// 此处是对文本框中绘制图片的方法：
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		// 当有提示信息时，显示提示图标
//		if (!StringUtilities.isNullOrEmpty(defaultWarningValue)) {
//			super.paintComponent(g);
////			int iconWidth = this.searchIcon.getIconWidth();
//			int iconHeight = this.searchIcon.getIconHeight();
//			this.searchIcon.paintIcon(this, g, 5, (this.getHeight() - (iconHeight - 1)) / 2);
//			this.setMargin(new Insets(0, 23, 0, 0));
//		} else {
//			super.paintComponent(g);
//			this.setMargin(new Insets(0, 0, 0, 0));
//		}
//		repaint();
//	}
}
