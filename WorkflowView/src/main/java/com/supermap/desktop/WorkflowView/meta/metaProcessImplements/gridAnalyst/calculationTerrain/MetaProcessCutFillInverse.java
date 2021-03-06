package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.gridAnalyst.calculationTerrain;

import com.supermap.analyst.spatialanalyst.CalculationTerrain;
import com.supermap.data.DatasetGrid;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.ProcessOutputResultProperties;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.datas.types.BasicTypes;
import com.supermap.desktop.process.parameter.ipls.ParameterCombine;
import com.supermap.desktop.process.parameter.ipls.ParameterComboBox;
import com.supermap.desktop.process.parameter.ipls.ParameterNumber;
import com.supermap.desktop.process.parameter.ipls.ParameterTextField;
import com.supermap.desktop.properties.CommonProperties;

/**
 * Created by ChenS on 2017/10/24 0024.
 */
public class MetaProcessCutFillInverse extends MetaProcessCalTerrain {
    private final static String OUTPUT_DATASET = "CutFillInverse";

    private ParameterComboBox comboBoxType;
    private ParameterNumber numberVolume;
    private ParameterTextField textFieldResult;

    public MetaProcessCutFillInverse() {
        setTitle(ProcessProperties.getString("String_Title_CutFillInverse"));
    }

    @Override
    protected void initHook() {
        comboBoxType = new ParameterComboBox(ProcessProperties.getString("String_CutFill_Type"));
        numberVolume = new ParameterNumber(ProcessProperties.getString("String_CutFillVolum"));
        textFieldResult = new ParameterNumber(ProcessOutputResultProperties.getString("String_Result_Height"));

        comboBoxType.setItems(new ParameterDataNode(ProcessProperties.getString("String_Fill"), true),
                new ParameterDataNode(ProcessProperties.getString("String_Cut"), false));
        numberVolume.setSelectedItem(1);
        numberVolume.setMinValue(0);
        numberVolume.setIsIncludeMin(true);
        textFieldResult.setEnabled(false);

        ParameterCombine parameterCombine = new ParameterCombine();
        parameterCombine.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));
        parameterCombine.addParameters(comboBoxType, numberVolume);

        parameterCombineResultDataset = new ParameterCombine();
        this.parameterCombineResultDataset.setDescribe(CommonProperties.getString("String_ResultSet"));
        parameterCombineResultDataset.addParameters(textFieldResult);

        parameters.addParameters(parameterCombine, parameterCombineResultDataset);
        parameters.addOutputParameters(OUTPUT_DATASET, ProcessOutputResultProperties.getString("String_Result_Height"), BasicTypes.DOUBLE, parameterCombineResultDataset);
    }

    @Override
    protected boolean doWork(DatasetGrid datasetGrid) {
        boolean isSuccessful = false;
        try {
            double volume = Double.parseDouble(numberVolume.getSelectedItem());
            boolean isFill = (boolean) comboBoxType.getSelectedData();
            double result = CalculationTerrain.cutFill(datasetGrid, volume, isFill);
            textFieldResult.setSelectedItem(result);
            parameters.getOutputs().getData(OUTPUT_DATASET).setValue(result);
            Application.getActiveApplication().getOutput().output(ProcessOutputResultProperties.getString("String_Result_Height") + result);
            isSuccessful = true;
        } catch (Exception e) {
            Application.getActiveApplication().getOutput().output(e);
        }

        return isSuccessful;
    }

    @Override
    protected String getDefaultResultName() {
        return null;
    }

    @Override
    public String getKey() {
        return MetaKeys.CUT_FILL_INVERSE;
    }
}
