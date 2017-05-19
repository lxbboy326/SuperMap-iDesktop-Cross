package com.supermap.desktop.dialog.cacheClip;

import com.mongodb.Mongo;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.supermap.data.*;
import com.supermap.data.processing.MapCacheBuilder;
import com.supermap.data.processing.MapTilingMode;
import com.supermap.data.processing.StorageType;
import com.supermap.desktop.Application;
import com.supermap.desktop.GlobalParameters;
import com.supermap.desktop.ScaleModel;
import com.supermap.desktop.dialog.SmOptionPane;
import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.ChooseTable.MultipleCheckboxItem;
import com.supermap.desktop.ui.controls.ChooseTable.MultipleCheckboxTableHeaderCellRenderer;
import com.supermap.desktop.ui.controls.ChooseTable.MultipleCheckboxTableRenderer;
import com.supermap.desktop.ui.controls.ChooseTable.SmChooseTable;
import com.supermap.desktop.ui.controls.*;
import com.supermap.desktop.ui.controls.ProviderLabel.WarningOrHelpProvider;
import com.supermap.desktop.ui.controls.mutiTable.DDLExportTableModel;
import com.supermap.desktop.ui.controls.mutiTable.component.MutiTable;
import com.supermap.desktop.utilities.*;
import com.supermap.mapping.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by xie on 2017/4/26.
 * Panel for clipping cache(first step)
 */
public class FirstStepPane extends JPanel implements IState {

    private final String urlStr = "/coreresources/ToolBar/";
    public MapCacheBuilder mapCacheBuilder;
    public HashMap<Double, String> globalSplitScale;
    public final int COLUMN_INDEX = 0;
    public final int COLUMN_SCALE = 1;
    public final int COLUMN_TITLE = 2;
    private String[] globalSplitTableTitle = {"", MapViewProperties.getString("MapCache_Scale")};
    public Double[] globalScaleSortKeys;
    private boolean mongoDBConnectSate;
    private Mongo mongo;
    private static final int DEFAULT_PORT = 27017;
    private static final String DEFAULT_ADDRESS = "localhost";
    private NumberFormat scientificNotation = NumberFormat.getInstance();
    private boolean textFieldServerNameGetFocus = false;
    private Vector<EnabledListener> enabledListeners;

    private JToolBar toolBar;
    public ComponentDropDown addScaleDropDown;
    private ComponentDropDown importDropDown;
    private ComponentDropDown exportDropDown;
    //basic setting
    private JButton buttonSelectAll;
    private JButton buttonSelectInverse;
    private JButton buttonDelete;
    private JMenuItem jMenuItemAddScale;
    private JMenuItem jMenuItemDefaultScale;
    private JMenuItem jMenuItemImportCacheConfigs;
    private JMenuItem jMenuItemImportScale;
    private JMenuItem jMenuItemExportCacheConfigs;
    private JMenuItem jMenuItemExportScale;
    public JScrollPane scrollPane;
    public MutiTable localSplitTable;
    public SmChooseTable globalSplitTable;
    private JLabel labelVersion;
    private JLabel labelSplitMode;
    private JLabel labelConfig;
    public JLabel labelConfigValue;
    private JComboBox comboboxVersion;
    private JComboBox comboBoxSplitMode;
    private JLabel labelCacheName;
    private JLabel labelCachePath;
    private WarningOrHelpProvider warningProviderCacheNameIllegal;
    private WarningOrHelpProvider warningProviderCachePathIllegal;
    public JTextField textFieldCacheName;

    private JLabel labelSaveType;
    private JLabel labelUserName;
    private JLabel labelUserPassword;
    private JLabel labelConfirmPassword;
    private JLabel labelServerName;
    private JLabel labelDatabaseName;
    private JLabel labelMutiTenseVersion;
    private WarningOrHelpProvider helpProvider;
    private WarningOrHelpProvider warningProviderPasswordNotSame;
    public JCheckBox checkBoxFilterSelectionObjectInLayer;
    public JComboBox comboBoxSaveType;
    public JComboBox comboBoxDatabaseName;
    private JComboBox comboBoxMutiTenseVersion;
    public JTextField textFieldUserName;
    public JPasswordField textFieldUserPassword;
    private JPasswordField textFieldConfirmPassword;
    public JTextField textFieldServerName;

    private double originMapCacheScale[];
    public ArrayList<Double> currentMapCacheScale;
    private static final String SHOW_SCALE_PRE_PART = "1:";
    private static final String COLON = ":";
    public boolean importCacheConfigs;//flag for recording if cache configs import or not

    public JFileChooserControl fileChooserControlFileCache;
    private Map currentMap;
    private DialogMapCacheClipBuilder parent;
    private ActionListener addScaleListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            addScale();
        }
    };
    private ActionListener addScaleToolbarJmenuListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            addScale();
        }
    };
    private ActionListener defaultScaleListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            addDefaultScale();
        }
    };

    private ActionListener inputCacheConfigFileListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            inportCacheConfigFile();
        }
    };
    private ActionListener inputCacheConfigFileToolbarJmenuListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            inportCacheConfigFile();
        }
    };
    private ActionListener exportCacheConfigFileListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            exportCacheConfigFile();
        }
    };
    private ActionListener exportCacheConfigFileToolbarJmenuListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            exportCacheConfigFile();
        }
    };

    private ActionListener exportScalesListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            exportXml();
        }
    };
    private ActionListener inputScalesListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            importXml();
        }
    };
    private ActionListener selectAllScaleListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (addScaleDropDown.isEnabled()) {
                localSplitTable.setRowSelectionInterval(0, localSplitTable.getRowCount() - 1);
            } else {
                globalSplitTable.setRowSelectionInterval(0, globalSplitTable.getRowCount() - 1);
            }
            checkButtonState();
        }
    };
    private ActionListener selectScaleInverseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectInvert();
            checkButtonState();
        }
    };

    private ActionListener deleteScaleListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            deleteScale();
            checkButtonState();
        }
    };
    private TableModelListener tableModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            tableModelChanged(e);
        }
    };

    private ItemListener saveTypeListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                initPanelImageSaveOutputDisplay(e.getItem().toString());
                fireEnabled(enabled());
            }
        }
    };
    private FileChooserPathChangedListener chooseCacheFilePathTextChangeListener = new FileChooserPathChangedListener() {
        @Override
        public void pathChanged() {
            fireEnabled(enabled());
        }
    };
    private DocumentListener passwordChangeListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }
    };

    public FirstStepPane(MapCacheBuilder mapCacheBuilder, DialogMapCacheClipBuilder parent) {
        super();
        this.parent = parent;
        this.mapCacheBuilder = mapCacheBuilder;
        this.currentMap = mapCacheBuilder.getMap();
        init();
    }

    private ItemListener comboboxSplitModeItemListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            splitModeChanged(e);
        }
    };

    private FocusListener serverNameFocusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (textFieldServerName.getText().equals(MapViewProperties.getString("MapCache_MongoDB_DefaultServerName"))) {
                textFieldServerName.setText("");
            }
            textFieldServerNameGetFocus = true;
            fireEnabled(enabled());
        }

        @Override
        public void focusLost(FocusEvent e) {
            textFieldServerNameGetFocus = false;
            connectMongoDBPretreatment();
            fireEnabled(enabled());
        }
    };
    private DocumentListener databaseNameChangeListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireEnabled(enabled());
        }
    };

    private void init() {
        initComponents();
        initLayout();
        initLocalSplitTable();
        initGlobalValue();
        initBasicSettingPanelValue();
        initResources();
        registEvents();
    }

    private void initLayout() {
        JPanel outputSetting = new JPanel();
        outputSetting.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("MapCache_OutputSetting")));
        JPanel pathSetting = new JPanel();
        pathSetting.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("MapCache_PathSetting")));
        outputSetting.setLayout(new GridBagLayout());
        outputSetting.add(this.labelVersion, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 10, 5, 10));
        outputSetting.add(this.comboboxVersion, new GridBagConstraintsHelper(1, 0, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 0, 5, 10).setWeight(1, 0));
        outputSetting.add(this.labelSplitMode, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        outputSetting.add(this.comboBoxSplitMode, new GridBagConstraintsHelper(1, 1, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        outputSetting.add(this.labelConfig, new GridBagConstraintsHelper(0, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        outputSetting.add(this.labelConfigValue, new GridBagConstraintsHelper(1, 2, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        pathSetting.setLayout(new GridBagLayout());
        pathSetting.add(this.labelCacheName, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 10, 5, 10));
        pathSetting.add(this.warningProviderCacheNameIllegal, new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 0, 5, 10));
        pathSetting.add(this.textFieldCacheName, new GridBagConstraintsHelper(2, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 0, 5, 10).setWeight(1, 0));
        pathSetting.add(this.labelCachePath, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        pathSetting.add(this.warningProviderCachePathIllegal, new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
        pathSetting.add(this.fileChooserControlFileCache, new GridBagConstraintsHelper(2, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        JPanel storeType = new JPanel();
        storeType.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("MapCache_OutputSetting")));
        storeType.setLayout(new GridBagLayout());
        storeType.add(this.labelSaveType, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 10, 5, 10));
        storeType.add(this.comboBoxSaveType, new GridBagConstraintsHelper(2, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.labelServerName, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 0));
        storeType.add(this.helpProvider, new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 0));
        storeType.add(this.textFieldServerName, new GridBagConstraintsHelper(2, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.labelDatabaseName, new GridBagConstraintsHelper(0, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        storeType.add(this.comboBoxDatabaseName, new GridBagConstraintsHelper(2, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.labelUserName, new GridBagConstraintsHelper(0, 3, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        storeType.add(this.textFieldUserName, new GridBagConstraintsHelper(2, 3, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.labelUserPassword, new GridBagConstraintsHelper(0, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        storeType.add(this.textFieldUserPassword, new GridBagConstraintsHelper(2, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10));
        storeType.add(this.labelMutiTenseVersion, new GridBagConstraintsHelper(0, 5, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10).setWeight(1, 0));
        storeType.add(this.comboBoxMutiTenseVersion, new GridBagConstraintsHelper(2, 5, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.labelConfirmPassword, new GridBagConstraintsHelper(0, 6, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        storeType.add(this.warningProviderPasswordNotSame, new GridBagConstraintsHelper(1, 6, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
        storeType.add(this.textFieldConfirmPassword, new GridBagConstraintsHelper(2, 6, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
        storeType.add(this.checkBoxFilterSelectionObjectInLayer, new GridBagConstraintsHelper(0, 7, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
        this.comboBoxDatabaseName.setEditable(true);
        JPanel panelRight = new JPanel();
        panelRight.setLayout(new GridBagLayout());
        panelRight.add(outputSetting, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5).setWeight(1, 0));
        panelRight.add(pathSetting, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 5, 5, 5).setWeight(1, 0));
        panelRight.add(storeType, new GridBagConstraintsHelper(0, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 5, 5, 5).setWeight(1, 0));
        panelRight.add(new JPanel(), new GridBagConstraintsHelper(0, 3, 1, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH).setWeight(1, 1));

        JPanel panelLeft = new JPanel();
        GroupLayout leftLayout = new GroupLayout(panelLeft);
        leftLayout.setHorizontalGroup(leftLayout.createParallelGroup().addComponent(this.toolBar, 260, 260, 260).addGap(5)
                .addComponent(this.scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        leftLayout.setVerticalGroup(leftLayout.createSequentialGroup().addComponent(this.toolBar, 30, 30, 30)
                .addComponent(this.scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        leftLayout.setAutoCreateContainerGaps(true);
        leftLayout.setAutoCreateGaps(true);
        panelLeft.setLayout(leftLayout);
        this.scrollPane.setViewportView(localSplitTable);

        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(panelLeft).addContainerGap().addComponent(panelRight));
        layout.setVerticalGroup(layout.createParallelGroup().addComponent(panelLeft).addComponent(panelRight));
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        this.setLayout(layout);

    }

    private void initBasicSettingPanelValue() {
        this.comboBoxSaveType.addItem(MapViewProperties.getString("MapCache_SaveType_Compact"));
        this.comboBoxSaveType.addItem(MapViewProperties.getString("MapCache_SaveType_Origin"));
        this.comboBoxSaveType.addItem(MapViewProperties.getString("MapCache_SaveType_MongoDB"));
        this.comboBoxSaveType.setSelectedItem(MapViewProperties.getString("MapCache_SaveType_Origin"));
        initPanelImageSaveOutputDisplay(MapViewProperties.getString("MapCache_SaveType_Origin"));
        this.comboboxVersion.addItem(MapViewProperties.getString("MapCache_ComboboxVersionItem"));
        this.comboBoxSplitMode.addItem(MapViewProperties.getString("MapCache_ComboboxSplitModeLocalSplit"));
        this.comboBoxSplitMode.addItem(MapViewProperties.getString("MapCache_ComboboxSplitModeGlobalSplit"));
        this.comboBoxSplitMode.setEnabled(false);
        if (this.currentMap.getPrjCoordSys() != null && this.currentMap.getPrjCoordSys().getType() == PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE
                && this.currentMap.getPrjCoordSys().getGeoCoordSys() != null && this.currentMap.getPrjCoordSys().getGeoCoordSys().getType() == GeoCoordSysType.GCS_WGS_1984
                && this.currentMap.getPrjCoordSys().getGeoCoordSys().getGeoSpatialRefType() == GeoSpatialRefType.SPATIALREF_EARTH_LONGITUDE_LATITUDE) {
            this.comboBoxSplitMode.setEnabled(true);
        }
        this.textFieldCacheName.setText(this.mapCacheBuilder.getMap().getName());
        String moduleName = "ChooseCacheDirectories";
        if (!SmFileChoose.isModuleExist(moduleName)) {
            this.fileChooserControlFileCache.setPath(System.getProperty("user.dir"));
        } else {
            SmFileChoose fileChoose = new SmFileChoose(moduleName);
            if (SystemPropertyUtilities.isWindows()) {
                if (!fileChoose.getModuleLastPath().substring(fileChoose.getModuleLastPath().length() - 1).equals("\\")) {
                    this.fileChooserControlFileCache.setPath(fileChoose.getModuleLastPath() + "\\");
                } else {
                    this.fileChooserControlFileCache.setPath(fileChoose.getModuleLastPath());
                }
            } else {
                if (!fileChoose.getModuleLastPath().substring(fileChoose.getModuleLastPath().length() - 1).equals("/")) {
                    this.fileChooserControlFileCache.setPath(fileChoose.getModuleLastPath() + "/");
                } else {
                    this.fileChooserControlFileCache.setPath(fileChoose.getModuleLastPath());
                }
            }
        }
        this.warningProviderCacheNameIllegal.hideWarning();
        this.warningProviderCachePathIllegal.hideWarning();
    }

    private void registEvents() {
        removeEvents();
        this.jMenuItemAddScale.addActionListener(this.addScaleListener);
        this.jMenuItemDefaultScale.addActionListener(this.defaultScaleListener);
        this.jMenuItemImportCacheConfigs.addActionListener(this.inputCacheConfigFileListener);
        this.jMenuItemExportCacheConfigs.addActionListener(this.exportCacheConfigFileListener);
        this.jMenuItemExportScale.addActionListener(this.exportScalesListener);
        this.jMenuItemImportScale.addActionListener(this.inputScalesListener);
        this.buttonSelectAll.addActionListener(this.selectAllScaleListener);
        this.buttonSelectInverse.addActionListener(this.selectScaleInverseListener);
        this.buttonDelete.addActionListener(this.deleteScaleListener);
        this.addScaleDropDown.getDisplayButton().addActionListener(this.addScaleToolbarJmenuListener);
        this.importDropDown.getDisplayButton().addActionListener(this.inputCacheConfigFileToolbarJmenuListener);
        this.exportDropDown.getDisplayButton().addActionListener(this.exportCacheConfigFileToolbarJmenuListener);
        this.localSplitTable.getModel().addTableModelListener(this.tableModelListener);
        this.comboBoxSplitMode.addItemListener(this.comboboxSplitModeItemListener);
        this.textFieldCacheName.getDocument().addDocumentListener(this.cacheNameTextChangeListener);
        this.comboBoxSaveType.addItemListener(this.saveTypeListener);
        this.textFieldServerName.addFocusListener(this.serverNameFocusListener);
        this.fileChooserControlFileCache.addFileChangedListener(this.chooseCacheFilePathTextChangeListener);
        ((JTextField) this.comboBoxDatabaseName.getEditor().getEditorComponent()).getDocument().addDocumentListener(this.databaseNameChangeListener);
        this.textFieldUserName.getDocument().addDocumentListener(this.passwordChangeListener);
        this.textFieldUserPassword.getDocument().addDocumentListener(this.passwordChangeListener);
        this.textFieldConfirmPassword.getDocument().addDocumentListener(this.passwordChangeListener);
    }

    public void removeEvents() {
        this.jMenuItemAddScale.removeActionListener(this.addScaleListener);
        this.jMenuItemDefaultScale.removeActionListener(this.defaultScaleListener);
        this.jMenuItemImportCacheConfigs.removeActionListener(this.inputCacheConfigFileListener);
        this.jMenuItemExportCacheConfigs.removeActionListener(this.exportCacheConfigFileListener);
        this.jMenuItemExportScale.removeActionListener(this.exportScalesListener);
        this.jMenuItemImportScale.removeActionListener(this.inputScalesListener);
        this.buttonSelectAll.removeActionListener(this.selectAllScaleListener);
        this.buttonSelectInverse.removeActionListener(this.selectScaleInverseListener);
        this.buttonDelete.removeActionListener(this.deleteScaleListener);
        this.addScaleDropDown.getDisplayButton().removeActionListener(this.addScaleToolbarJmenuListener);
        this.importDropDown.getDisplayButton().removeActionListener(this.inputCacheConfigFileToolbarJmenuListener);
        this.exportDropDown.getDisplayButton().removeActionListener(this.exportCacheConfigFileToolbarJmenuListener);
        this.localSplitTable.getModel().removeTableModelListener(this.tableModelListener);
        this.comboBoxSplitMode.removeItemListener(this.comboboxSplitModeItemListener);
        this.textFieldCacheName.getDocument().removeDocumentListener(this.cacheNameTextChangeListener);
        this.comboBoxSaveType.removeItemListener(this.saveTypeListener);
        this.textFieldServerName.removeFocusListener(this.serverNameFocusListener);
        this.fileChooserControlFileCache.removePathChangedListener(this.chooseCacheFilePathTextChangeListener);
        ((JTextField) this.comboBoxDatabaseName.getEditor().getEditorComponent()).getDocument().removeDocumentListener(this.databaseNameChangeListener);
        this.textFieldUserName.getDocument().removeDocumentListener(this.passwordChangeListener);
        this.textFieldUserPassword.getDocument().removeDocumentListener(this.passwordChangeListener);
        this.textFieldConfirmPassword.getDocument().removeDocumentListener(this.passwordChangeListener);
    }

    private void initPanelImageSaveOutputDisplay(String currentShowItem) {
        if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_Compact")) || currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_Origin")) || currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_GeoPackage"))) {
            this.labelServerName.setVisible(false);
            this.helpProvider.setVisible(false);
            this.textFieldServerName.setVisible(false);
            this.labelDatabaseName.setVisible(false);
            this.comboBoxDatabaseName.setVisible(false);
            this.labelUserName.setVisible(false);
            this.textFieldUserName.setVisible(false);
            this.labelMutiTenseVersion.setVisible(false);
            this.comboBoxMutiTenseVersion.setVisible(false);

            this.labelUserPassword.setVisible(true);
            this.labelConfirmPassword.setVisible(true);
            this.warningProviderPasswordNotSame.setVisible(true);
            this.warningProviderPasswordNotSame.hideWarning();
            this.textFieldUserPassword.setVisible(true);
            this.textFieldConfirmPassword.setVisible(true);
            this.textFieldUserPassword.setText("");
            this.textFieldConfirmPassword.setText("");
            if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_Compact"))) {
                this.textFieldUserPassword.setEnabled(true);
                this.textFieldConfirmPassword.setEnabled(true);
                this.mapCacheBuilder.setStorageType(StorageType.Compact);
            } else {
                this.textFieldUserPassword.setEnabled(false);
                this.textFieldConfirmPassword.setEnabled(false);
                if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_Origin"))) {
                    this.mapCacheBuilder.setStorageType(StorageType.Original);
                } else if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_GeoPackage"))) {
                    this.mapCacheBuilder.setStorageType(StorageType.GPKG);
                }
            }

        } else if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_MongoDB")) || currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_MongoDBMuti"))) {
            this.labelServerName.setVisible(true);
            this.helpProvider.setVisible(true);
            this.textFieldServerName.setVisible(true);
            this.textFieldServerName.setText(MapViewProperties.getString("MapCache_MongoDB_DefaultServerName"));
            this.labelDatabaseName.setVisible(true);
            this.comboBoxDatabaseName.setVisible(true);
            this.comboBoxDatabaseName.removeAllItems();
            this.labelUserName.setVisible(true);
            this.textFieldUserName.setVisible(true);
            this.textFieldUserName.setText("");
            this.textFieldUserPassword.setEnabled(true);
            this.textFieldUserPassword.setText("");
            this.labelConfirmPassword.setVisible(false);
            this.warningProviderPasswordNotSame.setVisible(false);
            this.textFieldConfirmPassword.setVisible(false);

            if (currentShowItem.equals(MapViewProperties.getString("MapCache_SaveType_MongoDBMuti"))) {
                this.labelMutiTenseVersion.setVisible(true);
                this.comboBoxMutiTenseVersion.setVisible(true);
                this.comboBoxMutiTenseVersion.removeAllItems();
            } else {
                this.labelMutiTenseVersion.setVisible(false);
                this.comboBoxMutiTenseVersion.setVisible(false);
                this.mapCacheBuilder.setStorageType(StorageType.MongoDB);
            }
            connectMongoDBPretreatment();
        }
    }

    // 非认证模式下连接mongodb，并读取数据库列表进行显示
    private void connectMongoDBPretreatment() {
        if (!this.textFieldServerName.getText().isEmpty()) {
            try {
                String address = "";
                Integer port = DEFAULT_PORT;
                if (this.textFieldServerName.getText().indexOf(COLON) != -1) {
                    String[] temp = this.textFieldServerName.getText().split(COLON);
                    if (!temp[0].isEmpty() && !temp[1].isEmpty() && temp[1].matches("[0-9]+")) {
                        address = temp[0];
                        port = Integer.valueOf(temp[1]);
                    } else {
                        this.comboBoxDatabaseName.removeAllItems();
                        return;
                    }
                } else {
                    address = this.textFieldServerName.getText();
                }
                connectMongoDB(address, port);
            } catch (Exception ex) {
                this.comboBoxDatabaseName.removeAllItems();
                return;
            }
        } else {
            connectMongoDB(DEFAULT_ADDRESS, DEFAULT_PORT);
            this.textFieldServerName.setText(MapViewProperties.getString("MapCache_MongoDB_DefaultServerName"));
        }
    }

    private void connectMongoDB(String address, Integer port) {
        try {
            ServerAddress serverAddress = new ServerAddress(address, port);
            Socket socket = new Socket();
            socket.connect(serverAddress.getSocketAddress(), 300);
        } catch (Exception e) {
            this.mongoDBConnectSate = false;
            this.comboBoxDatabaseName.removeAllItems();
            return;
        }
        Mongo.Holder holder = new Mongo.Holder();
        this.mongo = holder.connect(new MongoClientURI("mongodb://" + address));
        try {
            this.comboBoxDatabaseName.removeAllItems();
            java.util.List<String> databaseNames = mongo.getDatabaseNames();
            this.mongoDBConnectSate = true;
            for (int i = 0; i < databaseNames.size(); i++) {
                this.comboBoxDatabaseName.addItem(databaseNames.get(i));
            }
            this.textFieldUserName.setText("");
            this.textFieldUserPassword.setText("");
            this.textFieldUserName.setEnabled(false);
            this.textFieldUserPassword.setEnabled(false);
        } catch (Exception e) {
            this.textFieldUserName.setEnabled(true);
            this.textFieldUserPassword.setEnabled(true);
            this.textFieldUserName.setText("");
            this.textFieldUserPassword.setText("");
        }
    }


    private DocumentListener cacheNameTextChangeListener = new DocumentListener() {
        private void modifyPath() {
            String directories = fileChooserControlFileCache.getPath();
            if (!directories.endsWith(File.separator)) {
                directories += File.separator;
                fileChooserControlFileCache.setPath(directories);
            }
            fireEnabled(enabled());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            modifyPath();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            modifyPath();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            modifyPath();
        }
    };

    private boolean enabled() {
        return isRightCacheName() && isRightCachePath() && isAllRightSaveTypeSetting() && isEmptyUserNameAndUserPassword();
    }

    //Validate cache name is right
    private boolean isRightCacheName() {
        boolean result = true;
        if (this.textFieldCacheName.getText().isEmpty()) {
            result = false;
            this.warningProviderCacheNameIllegal.showWarning();
        } else {
            this.warningProviderCacheNameIllegal.hideWarning();
        }
        return result;
    }

    //Validate save type is right
    private boolean isAllRightSaveTypeSetting() {
        boolean result = true;
        if (this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_Compact"))) {
            result = isPasswordSame();
        } else if (this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_Origin")) || this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_GeoPackage"))) {

        } else if (this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_MongoDB")) || this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_MongoDBMuti"))) {
            if (this.comboBoxSaveType.getSelectedItem().toString().equals(MapViewProperties.getString("MapCache_SaveType_MongoDB"))) {
                if (textFieldServerNameGetFocus || !this.mongoDBConnectSate || this.comboBoxDatabaseName.getEditor().getItem().toString().isEmpty()) {
                    result = false;
                }
            } else {
                result = false;
            }
        }
        return result;
    }

    //Validate username and user password is empty
    private boolean isEmptyUserNameAndUserPassword() {
        boolean result = true;
        if (this.textFieldUserName.isVisible() && this.textFieldUserName.isEnabled() && this.textFieldUserPassword.isVisible() && this.textFieldUserPassword.isEnabled()) {
            if (this.textFieldUserName.getText().isEmpty() || this.textFieldUserPassword.getText().isEmpty()) {
                result = false;
            }
        }
        return result;
    }

    //Validate password is same
    private boolean isPasswordSame() {
        boolean result = true;
        if (this.textFieldConfirmPassword.getText().isEmpty() && this.textFieldUserPassword.getText().isEmpty()) {
            this.warningProviderPasswordNotSame.hideWarning();
            return result;
        }
        if (this.textFieldConfirmPassword.isVisible() && this.textFieldConfirmPassword.isEnabled() && !this.textFieldConfirmPassword.getText().isEmpty()) {
            if (!this.textFieldConfirmPassword.getText().equals(this.textFieldUserPassword.getText())) {
                result = false;
                this.warningProviderPasswordNotSame.showWarning();
            } else {
                this.warningProviderPasswordNotSame.hideWarning();
            }
        }
        return result;
    }

    //Validate is right cache path
    private boolean isRightCachePath() {
        boolean result = true;
        if (this.fileChooserControlFileCache.getPath().isEmpty()) {
            result = false;
            this.warningProviderCachePathIllegal.showWarning();
            return result;
        } else {
            File file = new File(this.fileChooserControlFileCache.getPath());
            if (!file.exists() && !file.isDirectory()) {
                result = false;
                this.warningProviderCachePathIllegal.showWarning(MapViewProperties.getString("MapCache_WarningCachePathIsNotExits"));
                return result;
            } else {
                this.warningProviderCachePathIllegal.hideWarning();
            }
        }
        return result;
    }

    private void initResources() {

        this.buttonSelectAll.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_SelectAll.png"));
        this.buttonSelectAll.setToolTipText(CommonProperties.getString("String_ToolBar_SelectAll"));
        this.buttonSelectInverse.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_SelectInverse.png"));
        this.buttonSelectInverse.setToolTipText(CommonProperties.getString("String_ToolBar_SelectInverse"));
        this.buttonDelete.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_Delete.png"));
        this.buttonDelete.setToolTipText(CommonProperties.getString("String_Delete"));

        this.labelVersion.setText(MapViewProperties.getString("MapCache_LabelVersion"));
        this.labelSplitMode.setText(MapViewProperties.getString("MapCache_LabelSplitMode"));
        this.labelConfig.setText(MapViewProperties.getString("MapCache_LabelConfig"));
        this.labelConfigValue.setText(MapViewProperties.getString("MapCache_LabelConfigValue"));
        this.labelCacheName.setText(MapViewProperties.getString("MapCache_LabelCacheName"));
        this.labelCachePath.setText(MapViewProperties.getString("MapCache_LabelCachePath"));
        this.labelSaveType.setText(MapViewProperties.getString("MapCache_SaveType"));
        this.labelUserName.setText(MapViewProperties.getString("MapCache_UserName"));
        this.labelUserPassword.setText(MapViewProperties.getString("MapCache_UserPassword"));
        this.labelConfirmPassword.setText(MapViewProperties.getString("MapCache_ConfirmPassword"));
        this.labelServerName.setText(MapViewProperties.getString("MapCache_ServerName"));
        this.labelDatabaseName.setText(MapViewProperties.getString("MapCache_DatabaseName"));
        this.labelMutiTenseVersion.setText(MapViewProperties.getString("MapCache_LabelVersion"));
        this.checkBoxFilterSelectionObjectInLayer.setText(MapViewProperties.getString("MapCache_FilterSelectObjectInLayer"));
    }

    private void initComponents() {
        this.enabledListeners = new Vector<>();
        this.scrollPane = new JScrollPane();
        this.localSplitTable = new MutiTable();
        this.jMenuItemAddScale = new JMenuItem(MapViewProperties.getString("MapCache_AddScale"), CoreResources.getIcon(urlStr + "Image_ToolButton_AddScale.png"));
        this.jMenuItemDefaultScale = new JMenuItem(MapViewProperties.getString("MapCache_DefaultScale"), CoreResources.getIcon(urlStr + "Image_ToolButton_DefaultScale.png"));
        this.jMenuItemImportCacheConfigs = new JMenuItem(MapViewProperties.getString("MapCache_ImportCacheConfigs"), CoreResources.getIcon(urlStr + "Image_ToolButton_ExportScale.png"));
        this.jMenuItemImportScale = new JMenuItem(MapViewProperties.getString("MapCache_ImportScale"), CoreResources.getIcon(urlStr + "Image_ToolButton_DefaultScale.png"));
        this.jMenuItemExportCacheConfigs = new JMenuItem(MapViewProperties.getString("MapCache_ExportCacheConfig"), CoreResources.getIcon(urlStr + "Image_ToolButton_ExportScale.png"));
        this.jMenuItemExportScale = new JMenuItem(MapViewProperties.getString("MapCache_ExportScale"), CoreResources.getIcon(urlStr + "Image_ToolButton_DefaultScale.png"));
        this.labelVersion = new JLabel();
        this.labelSplitMode = new JLabel();
        this.labelConfig = new JLabel();
        this.labelConfigValue = new JLabel();
        this.comboboxVersion = new JComboBox();
        this.comboBoxSplitMode = new JComboBox();
        this.labelCacheName = new JLabel();
        this.labelCachePath = new JLabel();
        this.warningProviderCacheNameIllegal = new WarningOrHelpProvider(MapViewProperties.getString("MapCache_WarningCacheNameIsEmpty"), true);
        this.warningProviderCachePathIllegal = new WarningOrHelpProvider(MapViewProperties.getString("MapCache_WarningCachePathIsEmpty"), true);
        this.textFieldCacheName = new JTextField();
        this.fileChooserControlFileCache = new JFileChooserControl();
        String moduleName = "ChooseCacheDirectories";
        if (!SmFileChoose.isModuleExist(moduleName)) {
            SmFileChoose.addNewNode("", System.getProperty("user.dir"), GlobalParameters.getDesktopTitle(),
                    moduleName, "GetDirectories");
        }

        SmFileChoose fileChoose = new SmFileChoose(moduleName);
        this.fileChooserControlFileCache.setFileChooser(fileChoose);
        this.labelSaveType = new JLabel();
        this.labelUserName = new JLabel();
        this.labelUserPassword = new JLabel();
        this.labelConfirmPassword = new JLabel();
        this.labelServerName = new JLabel();
        this.labelDatabaseName = new JLabel();
        this.labelMutiTenseVersion = new JLabel();
        this.helpProvider = new WarningOrHelpProvider(MapViewProperties.getString("MapCache_ServeNameHelp"), false);
        this.warningProviderPasswordNotSame = new WarningOrHelpProvider(MapViewProperties.getString("MapCache_PasswordIsNotSame"), true);
        this.checkBoxFilterSelectionObjectInLayer = new JCheckBox();
        this.checkBoxFilterSelectionObjectInLayer.setEnabled(false);
        this.comboBoxSaveType = new JComboBox();
        this.comboBoxDatabaseName = new JComboBox();
        this.comboBoxMutiTenseVersion = new JComboBox();
        this.textFieldUserName = new JTextField();
        this.textFieldUserPassword = new JPasswordField();
        this.textFieldConfirmPassword = new JPasswordField();
        this.textFieldServerName = new JTextField();
        initToolBar();
    }

    private void initGlobalValue() {
        this.mapCacheBuilder.setMap(this.currentMap);
        this.scientificNotation.setGroupingUsed(false);
        this.originMapCacheScale = this.mapCacheBuilder.getDefultOutputScales();
        setLocalSplitTableValue(this.originMapCacheScale);
        this.currentMapCacheScale = new ArrayList<>();
        for (int i = 0; i < this.originMapCacheScale.length; i++) {
            this.currentMapCacheScale.add(this.originMapCacheScale[i]);
        }
    }

    private void initLocalSplitTable() {
        DDLExportTableModel tableModel = new DDLExportTableModel(new String[]{"index", "scale", "title"}) {
            boolean[] columnEditables = new boolean[]{false, true, true};

            @Override
            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        };
        this.localSplitTable.setModel(tableModel);
        this.localSplitTable.setRowHeight(23);
        this.localSplitTable.getColumnModel().getColumn(COLUMN_INDEX).setMaxWidth(40);
        this.localSplitTable.getColumnModel().getColumn(COLUMN_TITLE).setMaxWidth(125);
        this.localSplitTable.getColumnModel().getColumn(COLUMN_TITLE).setMinWidth(125);
        this.localSplitTable.getColumnModel().getColumn(COLUMN_INDEX).setHeaderValue("");
        this.localSplitTable.getColumnModel().getColumn(COLUMN_SCALE).setHeaderValue(MapViewProperties.getString("MapCache_Scale"));
        this.localSplitTable.getColumnModel().getColumn(COLUMN_TITLE).setHeaderValue(MapViewProperties.getString("MapCache_ScaleTitle"));
    }

    private void setLocalSplitTableValue(double value[]) {
        try {
            for (int i = 0; i < value.length; i++) {
                Object[] temp = new Object[3];
                temp[COLUMN_INDEX] = i + 1 + " ";
                temp[COLUMN_SCALE] = SHOW_SCALE_PRE_PART + String.valueOf(DoubleUtilities.getFormatString(Double.valueOf(this.scientificNotation.format(1 / value[i]))));
                temp[COLUMN_TITLE] = (int) (Math.round(1 / value[i])) + " ";
                this.localSplitTable.addRow(temp);
            }
            addMoreScale();
            localSplitTable.setRowSelectionInterval(0, 0);
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        }
    }

    private void addMoreScale() {
        Object[] temp = new Object[3];
        temp[COLUMN_INDEX] = "*";
        temp[COLUMN_SCALE] = "";
        temp[COLUMN_TITLE] = "";
        this.localSplitTable.addRow(temp);
    }

    private void initToolBar() {
        this.toolBar = new JToolBar();
        this.addScaleDropDown = new ComponentDropDown(ComponentDropDown.IMAGE_TYPE);
        JPopupMenu popupMenuScale = new JPopupMenu();
        popupMenuScale.add(jMenuItemAddScale);
        popupMenuScale.add(jMenuItemDefaultScale);
        this.addScaleDropDown.setPopupMenu(popupMenuScale);
        this.addScaleDropDown.setToolTip(MapViewProperties.getString("String_AddScale"));
        this.addScaleDropDown.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_AddScale.png"));

        this.importDropDown = new ComponentDropDown(ComponentDropDown.IMAGE_TYPE);
        JPopupMenu popupMenuImport = new JPopupMenu();
        popupMenuImport.add(jMenuItemImportCacheConfigs);
        popupMenuImport.add(jMenuItemImportScale);
        this.importDropDown.setPopupMenu(popupMenuImport);
        this.importDropDown.setToolTip(MapViewProperties.getString("MapCache_Import"));
        this.importDropDown.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_Import.png"));

        this.exportDropDown = new ComponentDropDown(ComponentDropDown.IMAGE_TYPE);
        JPopupMenu popupMenuExport = new JPopupMenu();
        popupMenuExport.add(jMenuItemExportCacheConfigs);
        popupMenuExport.add(jMenuItemExportScale);
        this.exportDropDown.setPopupMenu(popupMenuExport);
        this.exportDropDown.setToolTip(MapViewProperties.getString("MapCache_Export"));
        this.exportDropDown.setIcon(CoreResources.getIcon(urlStr + "Image_ToolButton_Export.png"));

        this.buttonSelectAll = new JButton();
        this.buttonSelectInverse = new JButton();
        this.buttonDelete = new JButton();
        this.toolBar.add(this.addScaleDropDown);
        this.toolBar.addSeparator();
        this.toolBar.add(this.buttonSelectAll);
        this.toolBar.add(this.buttonSelectInverse);
        this.toolBar.add(this.buttonDelete);
        this.toolBar.addSeparator();
        this.toolBar.add(this.importDropDown);
        this.toolBar.add(this.exportDropDown);
        this.toolBar.setFloatable(false);
    }

    //Init global cache scales
    public void initGlobalCacheScales() {
        int[] levels = new int[21]; //Add global cache scale to 21
        for (int i = 0; i < 21; i++) {
            levels[i] = i;
        }
        this.globalSplitScale = this.mapCacheBuilder.globalLevelToScale(levels);
        double avaliableScale = -1;
        if (!this.mapCacheBuilder.getBounds().isEmpty() && this.mapCacheBuilder.getMap().getPrjCoordSys().getGeoCoordSys() != null) {
            avaliableScale = getAvaliableScale();
        }
        double dValue = Double.MAX_VALUE;
        String avaliableLevel = "0";
        if (avaliableScale != -1) {
            Object[] keys = this.globalSplitScale.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                double temp = Double.valueOf(keys[i].toString());
                double currentDValue = Math.abs(avaliableScale - (1.0 / temp));
                if (currentDValue < dValue) {
                    dValue = currentDValue;
                    avaliableLevel = this.globalSplitScale.get(temp);
                }
            }
        }
        initGlobalSplitTable();
        setGlobalSplitTableValue(avaliableLevel);
    }

    private void initGlobalSplitTable() {
        boolean[] columnEditables = new boolean[]{false, false};
        if (this.globalSplitTable == null) {
            this.globalSplitTable = new SmChooseTable(new Object[this.globalSplitScale.size()][2], globalSplitTableTitle, columnEditables);
        }
        this.globalSplitTable.getColumn(globalSplitTable.getModel().getColumnName(COLUMN_INDEX)).setMaxWidth(60);
        this.globalSplitTable.getColumnModel().getColumn(COLUMN_INDEX).setCellRenderer(new MultipleCheckboxTableRenderer());
        this.globalSplitTable.getTableHeader().getColumnModel().getColumn(COLUMN_INDEX).setHeaderRenderer(new MultipleCheckboxTableHeaderCellRenderer(this.globalSplitTable, MapViewProperties.getString("MapCache_LayerSeries"), true));
    }

    //Insert value into globalSplitTable
    private void setGlobalSplitTableValue(String avaliableLevel) {
        try {
            this.globalScaleSortKeys = this.globalSplitScale.keySet().toArray(new Double[this.globalSplitScale.size()]);
            double temp;
            for (int i = 0; i < globalScaleSortKeys.length - 1; i++) {
                for (int j = 0; j < globalScaleSortKeys.length - i - 1; j++) {
                    if (Double.compare(globalScaleSortKeys[j], globalScaleSortKeys[j + 1]) == 1) {
                        temp = globalScaleSortKeys[j];
                        globalScaleSortKeys[j] = globalScaleSortKeys[j + 1];
                        globalScaleSortKeys[j + 1] = temp;
                    }
                }
            }
            boolean isSelected = false;
            for (int i = 0; i < globalScaleSortKeys.length; i++) {
                temp = globalScaleSortKeys[i];
                MultipleCheckboxItem multipleCheckboxItem = new MultipleCheckboxItem();
                multipleCheckboxItem.setText(this.globalSplitScale.get(temp));
                if (this.globalSplitScale.get(temp).equals(avaliableLevel)) {
                    isSelected = true;
                }
                multipleCheckboxItem.setSelected(isSelected);
                this.globalSplitTable.setValueAt(multipleCheckboxItem, i, 0);
                this.globalSplitTable.setValueAt(this.SHOW_SCALE_PRE_PART + String.valueOf(DoubleUtilities.getFormatString(Double.valueOf(this.scientificNotation.format(1 / temp)))), i, COLUMN_SCALE);
            }
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        }
    }

    private double getAvaliableScale() {
        try {
            double DPI = mapCacheBuilder.getMap().getDPI();
            double lp = DPI / 25.4;
            Integer tileSize = Convert.toInteger(mapCacheBuilder.getTileSize().value());
            double dLPSide = tileSize / lp * 10;
            Unit mapUnit = mapCacheBuilder.getMap().getCoordUnit();
            Rectangle2D rcFinalBounds = mapCacheBuilder.getMap().getBounds();
            double dLPMPCoordRatio = dLPSide / rcFinalBounds.getWidth();
            double dScale = 0;
            double axis = mapCacheBuilder.getMap().getPrjCoordSys().getGeoCoordSys().getGeoDatum().getGeoSpheroid().getAxis();
            if (mapUnit == Unit.DEGREE) {
                dScale = dLPMPCoordRatio / (axis * 10000 * Math.PI / 180);
            } else {
                dScale = dLPMPCoordRatio / (Convert.toDouble(mapUnit));
            }
            return 1.0 / dScale;
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
            return 1.0;
        }
    }

    //Add new scale
    private void addScale() {
        int insertIndex = this.localSplitTable.getRowCount();
        if (this.localSplitTable.getRowCount() == 1) {
            this.currentMapCacheScale.add(this.originMapCacheScale[0]);
        } else {
            double insertScale = 0;
            int selectedRowsIndex[] = this.localSplitTable.getSelectedRows();
            if (selectedRowsIndex != null && selectedRowsIndex.length >= 1 && selectedRowsIndex[selectedRowsIndex.length - 1] + 1 != this.localSplitTable.getRowCount()) {
                insertIndex = selectedRowsIndex[selectedRowsIndex.length - 1] + 2;
            }
            if (insertIndex == this.localSplitTable.getRowCount()) {
                insertScale = this.currentMapCacheScale.get(insertIndex - 2) * 2;
            } else {
                insertScale = (this.currentMapCacheScale.get(insertIndex - 2) + this.currentMapCacheScale.get(insertIndex - 1)) / 2;
            }
            this.currentMapCacheScale.add(insertIndex - 1, insertScale);
        }
        double addScaleAfter[] = new double[this.currentMapCacheScale.size()];
        for (int i = 0; i < addScaleAfter.length; i++) {
            addScaleAfter[i] = this.currentMapCacheScale.get(i);
        }
        DDLExportTableModel model = (DDLExportTableModel) this.localSplitTable.getModel();
        model.removeRows(0, model.getRowCount());
        setLocalSplitTableValue(addScaleAfter);
        this.localSplitTable.setRowSelectionInterval(insertIndex - 1, insertIndex - 1);
        checkButtonState();
    }

    //Add default scale
    private void addDefaultScale() {
        DDLExportTableModel model = (DDLExportTableModel) localSplitTable.getModel();
        model.removeRows(0, model.getRowCount());
        setLocalSplitTableValue(originMapCacheScale);
        currentMapCacheScale.clear();
        for (int i = 0; i < originMapCacheScale.length; i++) {
            currentMapCacheScale.add(originMapCacheScale[i]);
        }
        localSplitTable.clearSelection();
    }

    //Import scales
    private void importXml() {
        try {
            String filePath = getFilePathForImport();
            this.currentMapCacheScale.clear();
            if (!StringUtilities.isNullOrEmpty(filePath)) {
                File file = new File(filePath);
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String tempstr = "";
                while ((tempstr = br.readLine()) != null) {
                    if (tempstr.contains("<Scale>")) {
                        tempstr = tempstr.substring(tempstr.indexOf(">") + 1, tempstr.lastIndexOf("<"));
                        String[] temp = tempstr.split(":");
                        this.currentMapCacheScale.add(1 / Double.valueOf(temp[1]));
                    }
                }
                double addScaleAfter[] = new double[this.currentMapCacheScale.size()];
                for (int i = 0; i < addScaleAfter.length; i++) {
                    addScaleAfter[i] = this.currentMapCacheScale.get(i);
                }
                DDLExportTableModel model = (DDLExportTableModel) this.localSplitTable.getModel();
                model.removeRows(0, model.getRowCount());
                setLocalSplitTableValue(addScaleAfter);
            }

        } catch (IOException e) {
            Application.getActiveApplication().getOutput().output(e);
        }
    }

    //Create a new FileChooser for import scales
    private String getFilePathForImport() {
        String filePath = "";
        String title = CommonProperties.getString("String_ToolBar_Import");
        if (!SmFileChoose.isModuleExist("ImportScale")) {
            String fileFilter = SmFileChoose.createFileFilter(MapViewProperties.getString("String_ScaleFile"), "xml");
            SmFileChoose.addNewNode(fileFilter, MapViewProperties.getString("String_ScaleFile"), title, "ImportScale", "OpenOne");
        }
        SmFileChoose fileChoose = new SmFileChoose("ImportScale");
        int stateTemp = fileChoose.showDefaultDialog();
        if (stateTemp == JFileChooser.APPROVE_OPTION) {
            filePath = fileChoose.getFilePath();
        }
        return filePath;
    }

    //Export scales
    protected void exportXml() {
        createXml(getFilePathForExport());
    }

    private void createXml(String filename) {
        try {
            DecimalFormat roundingTwoPoint = new DecimalFormat("#.00");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element scales = document.createElement("Scales");
            scales.setAttribute("xmlns", "http://www.supermap.com.cn/desktop");
            scales.setAttribute("version", "8.1.x");
            document.appendChild(scales);
            List<String> tempList = new ArrayList<String>();

            if (this.mapCacheBuilder.getTilingMode() == MapTilingMode.LOCAL) {
                for (int i = 0; i < this.localSplitTable.getRowCount() - 1; i++) {
                    String temp = this.localSplitTable.getValueAt(i, COLUMN_SCALE).toString();
                    tempList.add(i, SHOW_SCALE_PRE_PART + roundingTwoPoint.format(DoubleUtilities.stringToValue(temp.split(":")[1])).toString());
                }
            } else {
                for (int i = 0; i < this.globalSplitTable.getRowCount(); i++) {
                    String temp = this.globalSplitTable.getValueAt(i, COLUMN_SCALE).toString();
                    tempList.add(i, SHOW_SCALE_PRE_PART + roundingTwoPoint.format(DoubleUtilities.stringToValue(temp.split(":")[1])).toString());
                }
            }
            for (int i = 0; i < tempList.size(); i++) {
                Element scale = document.createElement("Scale");
                String scaleCaption = tempList.get(i);
                scale.appendChild(document.createTextNode(scaleCaption));
                scale.setNodeValue(scaleCaption);
                scales.appendChild(scale);
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if (StringUtilities.isNullOrEmpty(filename)) {
                return;
            }
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
                parseFileToXML(transformer, source, file);
            } else if (JOptionPane.OK_OPTION == new SmOptionPane().showConfirmDialog(MapViewProperties.getString("String_RenameFile_Message"))) {
                parseFileToXML(transformer, source, file);
            }
            if (file.exists() && file.length() > 0) {
                Application.getActiveApplication().getOutput()
                        .output(MessageFormat.format(MapViewProperties.getString("String_ExportScale_Scucess_Info"), file.getPath()));
            }
        } catch (Exception e) {
            Application.getActiveApplication().getOutput().output(e);
        }
    }

    private void parseFileToXML(Transformer transformer, DOMSource source, File file) throws FileNotFoundException, TransformerException {
        PrintWriter pw = new PrintWriter(file);
        StreamResult streamResult = new StreamResult(pw);
        transformer.transform(source, streamResult);
    }

    private String getFilePathForExport() {
        String filePath = "";
        if (!SmFileChoose.isModuleExist("ExportScale")) {
            String fileFilter = SmFileChoose.createFileFilter(MapViewProperties.getString("String_ScaleFile"), "xml");
            SmFileChoose.addNewNode(fileFilter, MapViewProperties.getString("String_ScaleFile"), CommonProperties.getString("String_ToolBar_Export"),
                    "ExportScale", "GetDirectories");
        }
        SmFileChoose tempfileChooser = new SmFileChoose("ExportScale");
        tempfileChooser.setSelectedFile(new File(MapViewProperties.getString("String_Scales") + ".xml"));
        int state = tempfileChooser.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            filePath = tempfileChooser.getFilePath();
        }
        return filePath;
    }

    private void exportCacheConfigFile() {
        try {
            String moduleName = "ExportCacheConfigFile";
            if (!SmFileChoose.isModuleExist(moduleName)) {
                String fileFilters = SmFileChoose.createFileFilter(MapViewProperties.getString("MapCache_CacheConfigFile"), "sci");
                SmFileChoose.addNewNode(fileFilters, CommonProperties.getString("String_DefaultFilePath"),
                        MapViewProperties.getString("String_SaveAsFile"), moduleName, "SaveOne");
            }
            SmFileChoose smFileChoose = new SmFileChoose(moduleName);
            smFileChoose.setSelectedFile(new File(MapViewProperties.getString("MapCache_CacheConfigFileIsNotbrackets")));
            int state = smFileChoose.showDefaultDialog();
            String filePath = "";
            if (state == JFileChooser.APPROVE_OPTION) {
                filePath = smFileChoose.getFilePath();
                File oleFile = new File(filePath);
                filePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".sci";
                File NewFile = new File(filePath);
                oleFile.renameTo(NewFile);
                if (oleFile.isFile() && oleFile.exists()) {
                    oleFile.delete();
                }
                this.mapCacheBuilder = parent.setMapCacheBuilderValueBeforeRun();
                boolean result = mapCacheBuilder.toConfigFile(filePath);
                if (result) {
                    Application.getActiveApplication().getOutput().output(MapViewProperties.getString("MapCache_ToCacheConfigFileIsSuccessed") + filePath);
                } else {
                    Application.getActiveApplication().getOutput().output(MapViewProperties.getString("MapCache_ToCacheConfigFileIsFailed"));
                }
            }
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex.toString());
        }
    }

    private void inportCacheConfigFile() {
        String moduleName = "InputCacheConfigFile";
        if (!SmFileChoose.isModuleExist(moduleName)) {
            String fileFilters = SmFileChoose.bulidFileFilters(SmFileChoose.createFileFilter(MapViewProperties.getString("MapCache_CacheConfigFile"), "sci"));
            SmFileChoose.addNewNode(fileFilters, CommonProperties.getString("String_DefaultFilePath"),
                    MapViewProperties.getString("String_OpenColorTable"), moduleName, "OpenMany");
        }
        SmFileChoose smFileChoose = new SmFileChoose(moduleName);
        int state = smFileChoose.showDefaultDialog();
        String filePath = "";
        if (state == JFileChooser.APPROVE_OPTION) {
            filePath = smFileChoose.getFilePath();
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            if (mapCacheBuilder.fromConfigFile(filePath)) {
                importCacheConfigs = true;
                HashMap<Double, String> scalesAndNames = mapCacheBuilder.getOutputScaleCaptions();
                if (mapCacheBuilder.getTilingMode() == MapTilingMode.LOCAL) {
                    comboBoxSplitMode.setSelectedItem(MapViewProperties.getString("MapCache_ComboboxSplitModeLocalSplit"));
                    DDLExportTableModel model = (DDLExportTableModel) localSplitTable.getModel();
                    model.removeRows(0, model.getRowCount());
                    setLocalSplitTableValue(scalesAndNames);
                } else {
                    comboBoxSplitMode.setSelectedItem(MapViewProperties.getString("MapCache_ComboboxSplitModeGlobalSplit"));
                    for (String scaleName : scalesAndNames.values()) {
                        MultipleCheckboxItem multipleCheckboxItem = (MultipleCheckboxItem) FirstStepPane.this.globalSplitTable.getValueAt(Integer.valueOf(scaleName), COLUMN_INDEX);
                        multipleCheckboxItem.setSelected(true);
                        FirstStepPane.this.globalSplitTable.setValueAt(multipleCheckboxItem, Integer.valueOf(scaleName), COLUMN_INDEX);
                    }
                }
                labelConfigValue.setText(fileName);
                textFieldCacheName.setText(mapCacheBuilder.getCacheName());
                fileChooserControlFileCache.setPath(mapCacheBuilder.getOutputFolder());
                if (mapCacheBuilder.getStorageType() == StorageType.Compact) {
                    comboBoxSaveType.setSelectedItem(MapViewProperties.getString("MapCache_SaveType_Compact"));
                } else if (mapCacheBuilder.getStorageType() == StorageType.Original) {
                    comboBoxSaveType.setSelectedItem(MapViewProperties.getString("MapCache_SaveType_Origin"));
                } else if (mapCacheBuilder.getStorageType() == StorageType.MongoDB) {
                    comboBoxSaveType.setSelectedItem(MapViewProperties.getString("MapCache_SaveType_MongoDB"));
                } else if (mapCacheBuilder.getStorageType() == StorageType.GPKG) {
                    comboBoxSaveType.setSelectedItem(MapViewProperties.getString("MapCache_SaveType_GeoPackage"));
                }
                Application.getActiveApplication().getOutput().output(MapViewProperties.getString("MapCache_FromCacheConfigFileIsSuccessed") + filePath);
            } else {
                Application.getActiveApplication().getOutput().output(MapViewProperties.getString("MapCache_FromCacheConfigFileIsFailed"));
            }
        }
    }

    private void selectInvert() {
        try {
            int[] temp = localSplitTable.getSelectedRows();
            ArrayList<Integer> selectedRows = new ArrayList();
            for (int index = 0; index < temp.length; index++) {
                selectedRows.add(temp[index]);
            }

            ListSelectionModel selectionModel = localSplitTable.getSelectionModel();
            selectionModel.clearSelection();
            for (int index = 0; index < localSplitTable.getRowCount(); index++) {
                if (!selectedRows.contains(index)) {
                    selectionModel.addSelectionInterval(index, index);
                }
            }
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        }
    }

    private void checkButtonState() {
        if (this.localSplitTable.getRowCount() > 1) {
            resetButtonState(true);
        } else {
            resetButtonState(false);
        }
        if (this.localSplitTable.getSelectedRows().length >= 1 && this.localSplitTable.getRowCount() > 1) {
            this.buttonDelete.setEnabled(true);
        } else {
            this.buttonDelete.setEnabled(false);
        }
    }

    private void resetButtonState(boolean enable) {
        this.buttonSelectAll.setEnabled(enable);
        this.buttonSelectInverse.setEnabled(enable);
        this.exportDropDown.setEnabled(enable);
    }

    private void setLocalSplitTableValue(HashMap<Double, String> scalesAndNames) {
        try {
            Double[] inputScales = scalesAndNames.keySet().toArray(new Double[scalesAndNames.size()]);
            for (int i = 0; i < inputScales.length - 1; i++) {
                for (int j = 0; j < inputScales.length - i - 1; j++) {
                    if (Double.compare(inputScales[j], inputScales[j + 1]) == 1) {
                        double temp = inputScales[j];
                        inputScales[j] = inputScales[j + 1];
                        inputScales[j + 1] = temp;
                    }
                }
            }
            this.currentMapCacheScale.clear();
            for (int i = 0; i < scalesAndNames.size(); i++) {
                Object[] temp = new Object[3];
                temp[COLUMN_INDEX] = i + 1 + " ";
                temp[COLUMN_SCALE] = SHOW_SCALE_PRE_PART + String.valueOf(DoubleUtilities.getFormatString(Double.valueOf(this.scientificNotation.format(1 / inputScales[i]))));
                temp[COLUMN_TITLE] = scalesAndNames.get(inputScales[i]);
                this.currentMapCacheScale.add(inputScales[i]);
                this.localSplitTable.addRow(temp);
            }
            addMoreScale();
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex.toString());
        }
    }

    private void deleteScale() {
        int selectedRowsIndex[] = localSplitTable.getSelectedRows();
        int tableCount = localSplitTable.getRowCount();
        List<Double> removeInfo = new ArrayList();
        localSplitTable.clearSelection();
        if (selectedRowsIndex != null & selectedRowsIndex.length >= 1) {
            for (int i = 0; i < selectedRowsIndex.length; i++) {
                if (selectedRowsIndex[i] + 1 != tableCount) {
                    removeInfo.add(currentMapCacheScale.get(selectedRowsIndex[i]));
                }
            }
        }
        if (removeInfo.size() > 0) {
            currentMapCacheScale.removeAll(removeInfo);
            double addScaleAfter[] = new double[currentMapCacheScale.size()];
            for (int i = 0; i < addScaleAfter.length; i++) {
                addScaleAfter[i] = currentMapCacheScale.get(i);
            }
            DDLExportTableModel model = (DDLExportTableModel) localSplitTable.getModel();
            model.removeRows(selectedRowsIndex);
            localSplitTable.refreshContents(getNewLocalSplitTableValue(addScaleAfter));
            addMoreScale();
            if (selectedRowsIndex != null) {
                int deleteAfaterSelectedRowIndex = selectedRowsIndex[0];
                if (deleteAfaterSelectedRowIndex + 1 == localSplitTable.getRowCount() && localSplitTable.getRowCount() > 1) {
                    localSplitTable.setRowSelectionInterval(deleteAfaterSelectedRowIndex - 1, deleteAfaterSelectedRowIndex - 1);
                } else {
                    localSplitTable.setRowSelectionInterval(deleteAfaterSelectedRowIndex, deleteAfaterSelectedRowIndex);
                }
            }
        }
    }

    private Object[][] getNewLocalSplitTableValue(double value[]) {
        Object[][] result = new Object[value.length][3];
        for (int i = 0; i < value.length; i++) {
            result[i][COLUMN_INDEX] = i + 1 + " ";
            result[i][COLUMN_SCALE] = SHOW_SCALE_PRE_PART + String.valueOf(DoubleUtilities.getFormatString(Double.valueOf(this.scientificNotation.format(1 / value[i]))));
            result[i][COLUMN_TITLE] = (int) (Math.round(1 / value[i])) + " ";
        }
        return result;
    }

    //Table model listener for localSplitTable
    private void tableModelChanged(TableModelEvent e) {
        int selectRow = e.getFirstRow();
        if (selectRow >= this.currentMapCacheScale.size()) {
            setTableCell(e.getFirstRow(), e.getColumn(), "");
            return;
        }
        String oldValue = SHOW_SCALE_PRE_PART + String.valueOf(1 / this.currentMapCacheScale.get(selectRow));
        String selectValue = localSplitTable.getValueAt(selectRow, e.getColumn()).toString();
        if (e.getColumn() == COLUMN_SCALE) {

            if (!ScaleModel.isLegitScaleString(selectValue)) {
                setTableCell(selectRow, COLUMN_SCALE, oldValue);
                Application.getActiveApplication().getOutput().output(MapViewProperties.getString("String_ErrorInput"));
                return;
            }
            if (ScaleModel.isLegitScaleString(selectValue) && selectValue.contains(COLON)) {
                setTableCell(selectRow, COLUMN_SCALE, selectValue);
                return;
            }
            if (!selectValue.contains(COLON) && ScaleModel.isLegitScaleString(SHOW_SCALE_PRE_PART + selectValue)) {
                setTableCell(selectRow, COLUMN_SCALE, SHOW_SCALE_PRE_PART + selectValue);
                return;
            }
        } else if (e.getColumn() == COLUMN_TITLE) {
            setTableCell(selectRow, COLUMN_TITLE, selectValue);
        }
        checkButtonState();
    }

    //Change table cell value
    private void setTableCell(int selectRow, int selectColumn, String value) {
        this.localSplitTable.getModel().removeTableModelListener(this.tableModelListener);
        if (selectColumn == COLUMN_SCALE && !value.equals("")) {
            this.localSplitTable.setValueAt(SHOW_SCALE_PRE_PART + DoubleUtilities.getFormatString(DoubleUtilities.stringToValue(value.split(":")[1])), selectRow, selectColumn);
            this.currentMapCacheScale.set(selectRow, 1 / DoubleUtilities.stringToValue(value.split(COLON)[1]));
        } else if (selectColumn == COLUMN_TITLE && !value.equals("")) {
            this.localSplitTable.setValueAt(value, selectRow, selectColumn);
        } else if (value.equals("")) {
            this.localSplitTable.setValueAt(value, selectRow, selectColumn);
        }
        this.localSplitTable.getModel().addTableModelListener(this.tableModelListener);
    }

    //ComboBoxSplitMode change listener
    private void splitModeChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (MapViewProperties.getString("MapCache_ComboboxSplitModeLocalSplit").equals(e.getItem().toString())) {
                mapCacheBuilder.setTilingMode(MapTilingMode.LOCAL);
                scrollPane.setViewportView(localSplitTable);
                addScaleDropDown.setEnabled(true);
                buttonDelete.setEnabled(true);
                importDropDown.setEnabled(true);
            } else {
                mapCacheBuilder.setTilingMode(MapTilingMode.GLOBAL);
                globalSplitTable = null;
                initGlobalCacheScales();
                scrollPane.setViewportView(globalSplitTable);
                addScaleDropDown.setEnabled(false);
                buttonDelete.setEnabled(false);
                importDropDown.setEnabled(false);
            }
        }
    }

    public void setComponentsEnabled(boolean enabled) {
        this.comboboxVersion.setEnabled(enabled);
        this.comboBoxSplitMode.setEnabled(enabled);
        this.textFieldCacheName.setEnabled(enabled);
        this.fileChooserControlFileCache.setEnabled(enabled);
        this.comboBoxSaveType.setEnabled(enabled);
        this.comboBoxDatabaseName.setEnabled(enabled);
        this.comboBoxMutiTenseVersion.setEnabled(enabled);
        this.textFieldUserName.setEnabled(enabled);
        this.textFieldUserPassword.setEnabled(enabled);
        this.textFieldConfirmPassword.setEnabled(enabled);
        this.textFieldServerName.setEnabled(enabled);
    }

    @Override
    public void addEnabledListener(EnabledListener enabledListener) {
        if (null == enabledListeners) {
            enabledListeners = new Vector<>();
        }
        if (null != enabledListener && !enabledListeners.contains(enabledListener)) {
            enabledListeners.add(enabledListener);
        }
    }

    @Override
    public void removeEnabledListener(EnabledListener enabledListener) {
        if (null != enabledListener && enabledListeners.contains(enabledListener)) {
            this.enabledListeners.remove(enabledListener);
        }

    }

    private void fireEnabled(boolean enabled) {
        Vector<EnabledListener> listeners = this.enabledListeners;
        for (EnabledListener listener : listeners) {
            listener.setEnabled(new EnabledEvent(enabled));
        }
    }
}