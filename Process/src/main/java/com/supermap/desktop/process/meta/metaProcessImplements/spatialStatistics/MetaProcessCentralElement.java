package com.supermap.desktop.process.meta.metaProcessImplements.spatialStatistics;

import com.supermap.analyst.spatialstatistics.SpatialMeasure;
import com.supermap.data.DatasetVector;
import com.supermap.desktop.Application;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.meta.MetaKeys;

/**
 * @author XiaJT
 */
public class MetaProcessCentralElement extends MetaProcessSpatialMeasure {

	public MetaProcessCentralElement() {
		super();
	}

	protected void initHook() {
		OUTPUT_DATASET = "CentralElementResult";
	}

	@Override
	protected void doWork(DatasetVector datasetVector) {
		try {
			SpatialMeasure.addSteppedListener(steppedListener);
// 调用中心要素方法，并获取结果矢量数据集
			DatasetVector result = SpatialMeasure.measureCentralElement(
					datasetVector,
					parameterSaveDataset.getResultDatasource(),
					parameterSaveDataset.getResultDatasource().getDatasets().getAvailableDatasetName(parameterSaveDataset.getDatasetName()),
					measureParameter.getMeasureParameter());
			this.getParameters().getOutputs().getData(OUTPUT_DATASET).setValue(result);

		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e.getMessage());
		} finally {
			SpatialMeasure.removeSteppedListener(steppedListener);
		}


	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_CentralElement");
	}

	@Override
	public String getKey() {
		return MetaKeys.CentralElement;
	}
}