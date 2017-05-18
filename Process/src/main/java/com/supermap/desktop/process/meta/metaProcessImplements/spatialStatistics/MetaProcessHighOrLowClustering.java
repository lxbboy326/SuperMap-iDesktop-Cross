package com.supermap.desktop.process.meta.metaProcessImplements.spatialStatistics;

import com.supermap.analyst.spatialstatistics.AnalyzingPatterns;
import com.supermap.analyst.spatialstatistics.AnalyzingPatternsResult;
import com.supermap.data.DatasetVector;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.meta.MetaKeys;
import com.supermap.desktop.process.parameter.implement.ParameterCombine;
import com.supermap.desktop.process.parameter.implement.ParameterTextArea;

/**
 * @author XiaJT
 */
public class MetaProcessHighOrLowClustering extends MetaProcessAnalyzingPatterns {
	private ParameterTextArea parameterResult;
	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_highOrLowClustering");
	}

	@Override
	protected void initHook() {
		parameterResult = new ParameterTextArea();
		ParameterCombine parameterCombine = new ParameterCombine();
		parameterCombine.addParameters(parameterResult);
		parameterCombine.setDescribe(ProcessProperties.getString("String_result"));
		parameters.addParameters(parameterCombine);
	}

	@Override
	protected void doWork(DatasetVector datasetVector) {
		AnalyzingPatternsResult analyzingPatternsResult = AnalyzingPatterns.highOrLowClustering(datasetVector, parameterPatternsParameter.getPatternParameter());
		String result = "";
		result += ProcessProperties.getString("String_GeneralG") + " " + analyzingPatternsResult.getIndex() + "\n";
		result += ProcessProperties.getString("String_Expectation") + " " + analyzingPatternsResult.getExpectation() + "\n";
		result += ProcessProperties.getString("String_Variance") + " " + analyzingPatternsResult.getVariance() + "\n";
		result += ProcessProperties.getString("String_ZScor") + " " + analyzingPatternsResult.getZScore() + "\n";
		result += ProcessProperties.getString("String_PValue") + " " + analyzingPatternsResult.getPValue() + "\n";
		parameterResult.setSelectedItem(result);
	}

	@Override
	public String getKey() {
		return MetaKeys.highOrLowClustering;
	}
}
