package com.supermap.desktop.CtrlAction.Map.MapMeasure.Measure;

import com.supermap.data.GeoCompound;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoRegion;
import com.supermap.data.GeoSpheroid;
import com.supermap.data.GeoStyle;
import com.supermap.data.GeoText;
import com.supermap.data.Geometrist;
import com.supermap.data.Geometry;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Rectangle2D;
import com.supermap.data.TextAlignment;
import com.supermap.data.TextPart;
import com.supermap.data.TextStyle;
import com.supermap.data.Unit;
import com.supermap.desktop.Application;
import com.supermap.desktop.enums.LengthUnit;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilties.FontUtilties;
import com.supermap.ui.Action;
import com.supermap.ui.TrackedEvent;
import com.supermap.ui.TrackedListener;
import com.supermap.ui.TrackingEvent;
import com.supermap.ui.TrackingListener;

import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * 距离量算
 */
public class MeasureDistance extends Measure {


	private double totleLength;

	private Geometry currentGeometry;
	private double lastPointX;
	private double lastPointY;
	private double currentX;
	private double currentY;


	private static final String measureLineTag = "measureLineTag";
	private String beforeUnit;

	public MeasureDistance() {
		super();
		textTagTitle = "DistanceText";
	}

	@Override
	protected void resetValue() {
		totleLength = 0;
	}

	@Override
	public void startMeasure() {
		super.startMeasure();
		mapControl.setAction(Action.CREATEPOLYLINE);
	}

	@Override
	protected void outputMeasure() {
		Application.getActiveApplication().getOutput().output(MessageFormat.format(CoreProperties.getString("String_Map_MeasureTotalDistance"), df.format(totleLength), getLengthUnit().toString()));
	}

	@Override
	protected void addListeners() {
		removeListeners();
		super.addListeners();
		this.mapControl.addTrackedListener(trackedListener);
		this.mapControl.addTrackingListener(trackingListener);
	}

	@Override
	protected void removeListeners() {
		super.removeListeners();
		this.mapControl.removeTrackedListener(trackedListener);
		this.mapControl.removeTrackingListener(trackingListener);
	}

	private final TrackingListener trackingListener = new TrackingListener() {
		@Override
		public void tracking(TrackingEvent trackingEvent) {
//			removeTrackingObject();
			currentGeometry = trackingEvent.getGeometry().clone();
			if (trackingEvent.getLength() > 0.0) {
				String unitString = getLengthUnit().toString();
				GeoLine geoLine = ((GeoLine) trackingEvent.getGeometry());
				Point2Ds points = geoLine.getPart(0);
				drawDistanceText(points, unitString, 1, true);
				Point2Ds pnts = new Point2Ds();
				pnts.add(points.getItem(points.getCount() - 2));
				pnts.add(points.getItem(points.getCount() - 1));

				// 构建折线的最后一个线段
				GeoLine geoLastSegment = new GeoLine(pnts);

				Point2D pntTemp = mapControl.getMap().pixelToMap(new Point(0, 0));
				Point2D pntTemp2 = mapControl.getMap().pixelToMap(new Point(0, assistantLineInterval));

				// 构造辅助线
				GeoLine geoLineAssistant = Geometrist.computeParallel(geoLastSegment, pntTemp2.getY() - pntTemp.getY());

				//辅助线风格
				GeoStyle geoStyle = new GeoStyle();
				geoStyle.setLineSymbolID(lineSymbolID);
				geoStyle.setLineWidth(lineWidth);
				geoStyle.setLineColor(lineColor);

				// 设置辅助线风格
				geoLineAssistant.setStyle(geoStyle);

				// 构造辅助线左端线段
				pnts.clear();
				pnts.add(geoLineAssistant.getPart(0).getItem(0));
				pnts.add(geoLastSegment.getPart(0).getItem(0));

				GeoLine geoLineLeft = new GeoLine(pnts);
				geoLineLeft.setStyle(geoStyle);

				// 构造辅助线右端线段
				pnts.clear();
				pnts.add(geoLineAssistant.getPart(0).getItem(1));
				pnts.add(geoLastSegment.getPart(0).getItem(1));

				GeoLine geoLineRight = new GeoLine(pnts);
				geoLineRight.setStyle(geoStyle);

				// 构造复合对象
				GeoCompound geoCompound = new GeoCompound();
				geoCompound.addPart(geoLineLeft);
				geoCompound.addPart(geoLineRight);
				geoCompound.addPart(geoLineAssistant);

				// 更新跟踪图层上的跟踪线信息
				int indexRecorder = indexOfTrackingObject();
				geoCompound.rotate(mapControl.getMap().getCenter(), -mapControl.getMap().getAngle());
				if (indexRecorder >= 0) {
					mapControl.getMap().getTrackingLayer().remove(indexRecorder);
					mapControl.getMap().getTrackingLayer().add(geoCompound, TRAKCING_OBJECT_NAME);
				} else {
					mapControl.getMap().getTrackingLayer().add(geoCompound, TRAKCING_OBJECT_NAME);
				}

				// 刷新跟踪图层
				mapControl.getMap().refreshTrackingLayer();

				// 根据地图显示范围对辅助线进行裁剪
				pnts.clear();
				Rectangle2D viewBounds = mapControl.getMap().getViewBounds();
				pnts.add(new Point2D(viewBounds.getLeft(), viewBounds.getBottom()));
				pnts.add(new Point2D(viewBounds.getLeft(), viewBounds.getTop()));
				pnts.add(new Point2D(viewBounds.getRight(), viewBounds.getTop()));
				pnts.add(new Point2D(viewBounds.getRight(), viewBounds.getBottom()));

				GeoRegion geoViewBounds = new GeoRegion(pnts);

				// 得到裁剪后的辅助线
				GeoLine geoClip = ((GeoLine) Geometrist.clip(geoLineAssistant, geoViewBounds));
				if (geoClip != null) {
					// 根据裁剪后的辅助线，计算编辑框显示的位置
					Point pntTextBox = mapControl.getMap().mapToPixel(geoClip.getInnerPoint());

					// 移动编辑框，并显示量算信息
					labelTextBoxCurrent.setLocation(pntTextBox);
				}

				setDistantTextBox(trackingEvent, unitString);
			}
		}
	};


	private void setDistantTextBox(TrackingEvent trackingEvent, String unitName) {
		try {

			PrjCoordSys prjCoordSys = mapControl.getMap().getPrjCoordSys();
			Double totalLength = LengthUnit.ConvertDistance(prjCoordSys, LengthUnit.getValueOf(unitName).getUnit(), trackingEvent.getTotalLength());
			Double curLength = LengthUnit.ConvertDistance(prjCoordSys, LengthUnit.getValueOf(unitName).getUnit(), trackingEvent.getLength());


			labelTextBoxCurrent.setText(MessageFormat.format(CoreProperties.getString("String_Map_MeasureCurrentDistance"), df.format(curLength), unitName));
			labelTextBoxCurrent.setSize(labelTextBoxCurrent.getText().length() * 10 + 18, 23);
			if (!labelTextBoxCurrent.isVisible()) {
				labelTextBoxCurrent.setVisible(true);
			}
			Point point = mapControl.getMap().mapToPixel(new Point2D(((int) trackingEvent.getX()), ((int) trackingEvent.getY())));

			double x = point.getX() + textBoxOffsetX;
			double y = point.getY() + textBoxOffsetY;

			if (x + labelTextBoxCurrent.getWidth() > mapControl.getWidth()) {
				x = x - labelTextBoxCurrent.getWidth();
			}
			if (y + labelTextBoxCurrent.getHeight() > mapControl.getHeight()) {
				y = y - labelTextBoxCurrent.getHeight() - textBoxOffsetY;
			}
			if (Math.abs(y - labelTextBoxCurrent.getBounds().getY()) < labelTextBoxCurrent.getHeight()) {
				y = y - labelTextBoxCurrent.getHeight() * 2;
			}

			Point pntTemp3 = new Point(((int) x), ((int) y));
			labelTextBoxTotle.setText(MessageFormat.format(CoreProperties.getString("String_Map_MeasureTotalDistance"), df.format(totalLength), unitName));
			labelTextBoxTotle.setSize(labelTextBoxTotle.getText().length() * 10 + 16, 23);
			labelTextBoxTotle.setLocation(pntTemp3);
			labelTextBoxTotle.setVisible(true);
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	private void drawDistanceText(Point2Ds points, String unitString, int param, boolean isDrawing) {
		if (addedTags == null) {
			addedTags = new ArrayList<>();
		}
		for (int i = 1; i < points.getCount() - param; i++) {
			String tag = textTagTitle + i;
			if (!addedTags.contains(tag)) {
				addedTags.add(tag);
				double distance;
				Point2D pntA = points.getItem(i - 1);
				Point2D pntB = points.getItem(i);
				Point2D pntMid = new Point2D((pntA.getX() + pntB.getX()) / 2, (pntA.getY() + pntB.getY()) / 2);
				Unit curDistanceUnit = LengthUnit.getValueOf(unitString).getUnit();
				if (mapControl.getMap().getPrjCoordSys().getType() == PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
					GeoSpheroid geoSpheroid = mapControl.getMap().getPrjCoordSys().getGeoCoordSys().getGeoDatum().getGeoSpheroid();
					distance = Geometrist.computeGeodesicDistance(new Point2Ds(new Point2D[]{pntA, pntB}), geoSpheroid.getAxis(),
							geoSpheroid.getFlatten());
					distance = LengthUnit.ConvertDistance(mapControl.getMap().getPrjCoordSys(), curDistanceUnit, distance);

				} else {
					Double x = pntB.getX() - pntA.getX();
					Double y = pntB.getY() - pntA.getY();
					distance = Math.sqrt(x * x + y * y);

					distance = LengthUnit.ConvertDistance(mapControl.getMap().getPrjCoordSys(), curDistanceUnit, distance);
				}
				totleLength += distance;

				String info = df.format(distance) + unitString;

				TextPart part = new TextPart(info, pntMid);
				GeoText geotext = new GeoText(part);

				TextStyle textStyle = geotext.getTextStyle();
				textStyle.setFontHeight(FontUtilties.fontSizeToMapHeight(textFontHeight * 0.283,
						mapControl.getMap(), textStyle.isSizeFixed()));
				textStyle.setAlignment(TextAlignment.BOTTOMLEFT);
				textStyle.setBackColor(Color.BLACK);
				textStyle.setForeColor(Color.WHITE);
				textStyle.setOutline(true);
				if (isDrawing) {
					mapControl.getMap().getTrackingLayer().add(geotext, tag);
				} else {
					mapControl.getMap().getTrackingLayer().add(geotext, tag + "FinishedMeasure");
				}
			} else {
				if (!isDrawing) {
					int index = mapControl.getMap().getTrackingLayer().indexOf(tag);
					if (index > -1) {
						mapControl.getMap().getTrackingLayer().setTag(index, tag + "FinishedMeasure");
					}
				}
			}
		}

		beforeUnit = unitString;
		mapControl.getMap().refreshTrackingLayer();
	}


	private TrackedListener trackedListener = new TrackedListener() {
		@Override
		public void tracked(TrackedEvent e) {
			try {
				if (e.getGeometry() != null) {
					GeoStyle geoStyle = e.getGeometry().getStyle();
					if (geoStyle == null) {
						geoStyle = new GeoStyle();
					}
					geoStyle.setLineWidth(0.1);
					geoStyle.setFillSymbolID(1);
					geoStyle.setLineColor(Color.BLUE);

					mapControl.getMap().getTrackingLayer().add(e.getGeometry(), measureLineTag);

					String unitString = getLengthUnit().toString();

					GeoLine geoLine = ((GeoLine) e.getGeometry());
					Point2Ds points = geoLine.getPart(0);

					drawDistanceText(points, unitString, 0, false);
					outputMeasure();
					resetValue();
				}
				cancleEdit();
				mapControl.getMap().refresh();
			} catch (Exception ex) {
				Application.getActiveApplication().getOutput().output(ex);
			}
		}
	};
}