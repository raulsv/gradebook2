/**********************************************************************************
*
* $Id:$
*
***********************************************************************************
*
* Copyright (c) 2008, 2009 The Regents of the University of California
*
* Licensed under the
* Educational Community License, Version 2.0 (the "License"); you may
* not use this file except in compliance with the License. You may
* obtain a copy of the License at
* 
* http://www.osedu.org/licenses/ECL-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an "AS IS"
* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing
* permissions and limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.gradebook.gwt.client.gxt.view.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.Gradebook2RPCServiceAsync;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.SecureToken;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StatisticsModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.GradeType;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class StudentPanel extends ContentPanel {

	private static final int GT_COL_ANAM = 0; 
	private static final int GT_COL_GRADE = 1; 
	private static final int GT_COL_MEAN = 2; 
	private static final int GT_COL_STDV = 3; 
	private static final int GT_COL_MEDI = 4; 
	private static final int GT_COL_MODE = 5; 
	private static final int GT_COL_RANK = 6; 
	private static final int GT_COL_COMM = 7; 

	private enum Key { CATEGORY_NAME, ITEM_NAME, GRADE, MEAN, STDV, MEDI, MODE, RANK, COMMENT, ORDER, ID };
	
	private TextField<String> defaultTextField= new TextField<String>();
	private TextArea defaultTextArea = new TextArea();
	private NumberFormat defaultNumberFormat = NumberFormat.getFormat("#.###");
	private NumberField defaultNumberField = new NumberField();
    private FlexTable studentInformation;
    private ContentPanel studentInformationPanel, gradeInformationPanel, textPanel;
    private Html textNotification;
    private LayoutContainer cardLayoutContainer;
    private CardLayout cardLayout;
    private FormPanel commentsPanel;
    private Grid<BaseModel> grid;
    private GroupingStore<BaseModel> store;
    private ColumnModel cm;
    private FormBinding formBinding;
    private GridSelectionModel<BaseModel> selectionModel;
    private TextArea commentArea;
	private StudentModel learnerGradeRecordCollection;
	
	private boolean isStudentView;
	
	private boolean displayRank; 
	
	private GradebookModel selectedGradebook;
	
	private boolean isPossibleStatsChanged = false;
	
	private List<StatisticsModel> statsList;
	
	public StudentPanel(I18nConstants i18n, boolean isStudentView, boolean displayRank) {
		this.isStudentView = isStudentView;
		this.defaultNumberField.setFormat(defaultNumberFormat);
		this.defaultNumberField.setSelectOnFocus(true);
		this.defaultNumberField.addInputStyleName("gbNumericFieldInput");
		this.defaultTextArea.addInputStyleName("gbTextAreaInput");
		this.defaultTextField.addInputStyleName("gbTextFieldInput");
		this.displayRank = displayRank;
		this.statsList = null;
		setFrame(true);
		setHeaderVisible(false);
		setLayout(new RowLayout());

		studentInformation = new FlexTable(); 
		studentInformation.setStyleName("gbStudentInformation");
		studentInformationPanel = new ContentPanel();
		studentInformationPanel.setBorders(true);
		studentInformationPanel.setFrame(true);
		studentInformationPanel.setHeaderVisible(false);
		studentInformationPanel.setLayout(new FitLayout());
		studentInformationPanel.setScrollMode(Scroll.AUTO);
		studentInformationPanel.add(studentInformation);
		add(studentInformationPanel, new RowData(-1, -1, new Margins(5, 0, 0, 0)));

		store = new GroupingStore<BaseModel>();
		store.setGroupOnSort(false);
		store.setSortField(Key.ORDER.name());
		store.setSortDir(Style.SortDir.ASC);
		store.setModelComparer(new ModelComparer<BaseModel>() {

			public boolean equals(BaseModel m1, BaseModel m2) {
				if (m1 == null || m2 == null)
					return false;
				
				String id1 = m1.get(Key.ID.name());
				String id2 = m2.get(Key.ID.name());
				
				if (id1 != null && id2 != null) {
					return id1.equals(id2);
				}
				
				return false;
			}
			
		});
		store.setStoreSorter(new StoreSorter<BaseModel>() {
			
			public int compare(Store<BaseModel> store, BaseModel m1, BaseModel m2, String property) {
			    if (property != null) {
			    	
			    	// We do not want the sort by category to take
			    	if (property.equals(Key.CATEGORY_NAME.name()))
			    		return 0;
			    	
			    	if (property.equals(Key.ITEM_NAME.name()))
			    		property = Key.ORDER.name();
			    		
			    	Object v1 = m1.get(property);
			    	Object v2 = m2.get(property);
			    	return comparator.compare(v1, v2);
			    }
			    return comparator.compare(m1, m2);
			}
			
		});
		
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig(Key.CATEGORY_NAME.name(), i18n.categoryName(), 200);
		column.setGroupable(true);
		column.setHidden(true);
		columns.add(column);
		
		column = new ColumnConfig(Key.ITEM_NAME.name(), i18n.itemName(), 200);
		column.setGroupable(false);
		columns.add(column);
		
		column = new ColumnConfig(Key.GRADE.name(), i18n.scoreName(), 180);
		column.setGroupable(false);
		columns.add(column);
		
		column = new ColumnConfig(Key.MEAN.name(), i18n.meanName(), 80);
		column.setGroupable(false);
		columns.add(column);
		
		column = new ColumnConfig(Key.STDV.name(), i18n.stdvName(), 80);
		column.setGroupable(false);
		columns.add(column);
		
		column = new ColumnConfig(Key.MEDI.name(), i18n.medianName(), 80);
		column.setGroupable(false);
		columns.add(column);
		
		column = new ColumnConfig(Key.MODE.name(), i18n.modeName(), 80);
		column.setGroupable(false);
		columns.add(column);
		
		cm = new ColumnModel(columns);
		
		selectionModel = new GridSelectionModel<BaseModel>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<BaseModel> sce) {
				BaseModel score = sce.getSelectedItem();

				if (score != null) {
					formBinding.bind(score);
					commentsPanel.show();
				} else {
					commentsPanel.hide();
					formBinding.unbind();
				}
			}

		});
		
		GroupingView view = new GroupingView();

		grid = new Grid<BaseModel>(store, cm);
		grid.setBorders(true);
		grid.setSelectionModel(selectionModel);
		grid.setView(view);

		
		cardLayoutContainer = new LayoutContainer();
		cardLayout = new CardLayout();
		cardLayoutContainer.setLayout(cardLayout);
		
		
		gradeInformationPanel = new ContentPanel() {
			
			protected void onResize(final int width, final int height) {
				super.onResize(width, height);
				
				grid.setSize(width - 400, height - 50);
				
				commentArea.setHeight(height - 84);
			}
			
		};
		gradeInformationPanel.setBorders(true);
		gradeInformationPanel.setFrame(true);
		gradeInformationPanel.setHeaderVisible(false);
		gradeInformationPanel.setLayout(new ColumnLayout());
		gradeInformationPanel.add(grid, new ColumnData(1));
		
		FormLayout commentLayout = new FormLayout();
		commentLayout.setLabelAlign(LabelAlign.TOP);
		commentLayout.setDefaultWidth(320);
		
		commentsPanel = new FormPanel();
		commentsPanel.setHeaderVisible(false);
		commentsPanel.setLayout(commentLayout);
		commentsPanel.setVisible(false);
		commentsPanel.setWidth(400);
		
		commentArea = new TextArea();
		commentArea.setName(Key.COMMENT.name());
		commentArea.setFieldLabel(i18n.commentName());
		commentArea.setHeight(300);
		commentArea.setReadOnly(true);
		commentsPanel.add(commentArea);
		
		gradeInformationPanel.add(commentsPanel, new ColumnData(400));
		
		formBinding = new FormBinding(commentsPanel, true);

		textPanel = new ContentPanel();
		textPanel.setBorders(true);
		textPanel.setFrame(true);
		textPanel.setHeaderVisible(false);
		
		textNotification = textPanel.addText("");
		
		cardLayoutContainer.add(gradeInformationPanel);
		cardLayoutContainer.add(textPanel);
		cardLayout.setActiveItem(gradeInformationPanel);
		
		add(cardLayoutContainer, new RowData(1, 1, new Margins(5, 0, 0, 0)));
	}
	
	public StudentModel getStudentRow() {
		return learnerGradeRecordCollection;
	}
	
	public void onRefreshGradebookSetup(GradebookModel selectedGradebook) {
		this.selectedGradebook = selectedGradebook;
        String overrideString = learnerGradeRecordCollection.get(StudentModel.Key.IS_GRADE_OVERRIDDEN.name()); 
        
		updateCourseGrade(learnerGradeRecordCollection.getLetterGrade(), overrideString, learnerGradeRecordCollection.getCalculatedGrade());
		
		List<StatisticsModel> statsList = selectedGradebook.getStatsModel();
		refreshGradeData(learnerGradeRecordCollection, statsList);
	}
	
	public void onChangeModel(GradebookModel selectedGradebook, final StudentModel learnerGradeRecordCollection) {
		if (learnerGradeRecordCollection != null) {
			this.selectedGradebook = selectedGradebook;
			this.learnerGradeRecordCollection = learnerGradeRecordCollection;
			String overrideString = learnerGradeRecordCollection.get(StudentModel.Key.IS_GRADE_OVERRIDDEN.name()); 
			
			updateCourseGrade(learnerGradeRecordCollection.getLetterGrade(), overrideString, learnerGradeRecordCollection.getCalculatedGrade());
			
			if (isPossibleStatsChanged) {
				AsyncCallback<ListLoadResult<StatisticsModel>> callback = new AsyncCallback<ListLoadResult<StatisticsModel>>() {

					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}

					public void onSuccess(ListLoadResult<StatisticsModel> result) {
						statsList = result.getData();
						refreshGradeData(learnerGradeRecordCollection, statsList);
						isPossibleStatsChanged = false;
					}
					
				};
				
				Gradebook2RPCServiceAsync service = Registry.get(AppConstants.SERVICE);
				service.getPage(selectedGradebook.getGradebookUid(), selectedGradebook.getGradebookId(), EntityType.STATISTICS, null, SecureToken.get(), callback);
			} else {
				if (statsList == null)
					statsList = selectedGradebook.getStatsModel();
				
				refreshGradeData(learnerGradeRecordCollection, statsList);
			}
		}
	}
	
	public void onItemUpdated(ItemModel itemModel) {

	}
	
	public void onLearnerGradeRecordUpdated(StudentModel learnerGradeRecordModel) {
		this.isPossibleStatsChanged = true;
	}
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

	}
	
	public void onResize(int x, int y) {
		super.onResize(x, y);
		

	}
	
	public void refreshColumns() {

	}
	
	public void refreshData() {

	}

	
	private void updateCourseGrade(String newGrade, String overrideString, String calcGrade)
	{
		if (!isStudentView || (selectedGradebook != null && selectedGradebook.getGradebookItemModel() != null 
				&& DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseGrades()))) {
			// To force a refresh, let's first hide the owning panel
			studentInformationPanel.hide();
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, newGrade);
			studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, calcGrade);
			studentInformationPanel.show();
		} else {
			studentInformationPanel.hide();
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_HEADING, "");
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, "");
			studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, "");
			studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, "");
			studentInformationPanel.show();
		}
		
		if (learnerGradeRecordCollection != null)
			learnerGradeRecordCollection.set(StudentModel.Key.COURSE_GRADE.name(), newGrade);
	}
	
	private void refreshGradeData(StudentModel learnerGradeRecordCollection, List<StatisticsModel> statsList) {
		StatisticsModel m = getStatsModelForItem(String.valueOf(Long.valueOf(-1)), statsList);
		setStudentInfoTable(m);
		setGradeInfoTable(selectedGradebook, learnerGradeRecordCollection, statsList);
	}
	
	
	// FIXME - i18n 
	// FIXME - need to assess impact of doing it this way... 
	
	
	private static final int PI_ROW_NAME = 1; 
	private static final int PI_ROW_EMAIL = 2; 
	private static final int PI_ROW_ID = 3; 
	private static final int PI_ROW_SECTION = 4; 
	private static final int PI_ROW_BLANK = 5; 
	private static final int PI_ROW_COURSE_GRADE = 6; 
	private static final int PI_ROW_CALCULATED_GRADE = 7; 
	private static final int PI_ROW_STATS = 1;
	private static final int PI_ROW_MEAN = 2; 
	private static final int PI_ROW_STDV = 3; 
	private static final int PI_ROW_MEDI = 4; 
	private static final int PI_ROW_MODE = 5; 
	private static final int PI_ROW_RANK = 6; 
	
	
	private static final int PI_COL_HEADING = 0; 
	private static final int PI_COL_VALUE = 1; 
	private static final int PI_COL2_HEADING = 2;
	private static final int PI_COL2_VALUE = 3;
	
	
	private void setStudentInfoTable(StatisticsModel courseGradeStats) {		
		// To force a refresh, let's first hide the owning panel
		studentInformationPanel.hide();
	
		// Now, let's update the student information table
		FlexCellFormatter formatter = studentInformation.getFlexCellFormatter();
		
        studentInformation.setText(PI_ROW_NAME, PI_COL_HEADING, "Name");
        formatter.setStyleName(PI_ROW_NAME, PI_COL_HEADING, "gbImpact");
        formatter.setWordWrap(PI_ROW_NAME, PI_COL_HEADING, false);
        studentInformation.setText(PI_ROW_NAME, PI_COL_VALUE, learnerGradeRecordCollection.getStudentName());

        studentInformation.setText(PI_ROW_EMAIL, PI_COL_HEADING, "Email");
        formatter.setStyleName(PI_ROW_EMAIL, PI_COL_HEADING, "gbImpact");
        formatter.setWordWrap(PI_ROW_EMAIL, PI_COL_HEADING, false);
        studentInformation.setText(PI_ROW_EMAIL, PI_COL_VALUE, learnerGradeRecordCollection.getStudentEmail());

        studentInformation.setText(PI_ROW_ID, PI_COL_HEADING, "Id");
        formatter.setStyleName(PI_ROW_ID, PI_COL_HEADING, "gbImpact");
        formatter.setWordWrap(PI_ROW_ID, PI_COL_HEADING, false);
        studentInformation.setText(PI_ROW_ID, PI_COL_VALUE, learnerGradeRecordCollection.getStudentDisplayId());

        studentInformation.setText(PI_ROW_SECTION, PI_COL_HEADING, "Section");
        formatter.setStyleName(PI_ROW_SECTION, PI_COL_HEADING, "gbImpact");
        formatter.setWordWrap(PI_ROW_SECTION, PI_COL_HEADING, false);
        studentInformation.setText(PI_ROW_SECTION, PI_COL_VALUE, learnerGradeRecordCollection.getStudentSections());
    
        studentInformation.setText(PI_ROW_BLANK, PI_COL_HEADING, "");
        studentInformation.setText(PI_ROW_BLANK, PI_COL_VALUE, "");
        //formatter.setColSpan(PI_ROW_BLANK, PI_COL_HEADING, 2);
        
        if (!isStudentView || (DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseGrades()))) {
	        studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_HEADING, "Course Grade");
	        formatter.setStyleName(PI_ROW_COURSE_GRADE, PI_COL_HEADING, "gbImpact");
	        studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, learnerGradeRecordCollection.getLetterGrade());
	        
	        boolean isLetterGrading = selectedGradebook.getGradebookItemModel().getGradeType() == GradeType.LETTERS;
	        
	        if (!isLetterGrading)
	        {
	        	String calculatedGrade = learnerGradeRecordCollection.getCalculatedGrade();
		        studentInformation.setHTML(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, "Calculated Grade");
		        formatter.setStyleName(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, "gbImpact");
		        formatter.setWordWrap(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, false);
		        studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, calculatedGrade);
	        }
	        if (courseGradeStats != null)
	        {
	        	studentInformation.setText(PI_ROW_STATS, PI_COL2_HEADING, "Course Statistics");
	        	formatter.setStyleName(PI_ROW_STATS, PI_COL2_HEADING, "gbHeading");
	        	
		        studentInformation.setText(PI_ROW_MEAN, PI_COL2_HEADING, "Mean");
		        formatter.setStyleName(PI_ROW_MEAN, PI_COL2_HEADING, "gbImpact");
		        formatter.setWordWrap(PI_ROW_MEAN, PI_COL2_HEADING, false);
		        studentInformation.setText(PI_ROW_MEAN, PI_COL2_VALUE, courseGradeStats.getMean());

		        studentInformation.setText(PI_ROW_STDV, PI_COL2_HEADING, "Standard Deviation");
		        formatter.setStyleName(PI_ROW_STDV, PI_COL2_HEADING, "gbImpact");
		        formatter.setWordWrap(PI_ROW_STDV, PI_COL2_HEADING, false);
		        studentInformation.setText(PI_ROW_STDV, PI_COL2_VALUE, courseGradeStats.getStandardDeviation());

		        studentInformation.setText(PI_ROW_MEDI, PI_COL2_HEADING, "Median");
		        formatter.setStyleName(PI_ROW_MEDI, PI_COL2_HEADING, "gbImpact");
		        studentInformation.setText(PI_ROW_MEDI, PI_COL2_VALUE, courseGradeStats.getMedian());

		        studentInformation.setText(PI_ROW_MODE, PI_COL2_HEADING, "Mode");
		        formatter.setStyleName(PI_ROW_MODE, PI_COL2_HEADING, "gbImpact");
		        studentInformation.setText(PI_ROW_MODE, PI_COL2_VALUE, courseGradeStats.getMode());
		        if (displayRank)
		        {
		        	studentInformation.setText(PI_ROW_RANK, PI_COL2_HEADING, "Rank");
		        	formatter.setStyleName(PI_ROW_RANK, PI_COL2_HEADING, "gbImpact");
		        	studentInformation.setText(PI_ROW_RANK, PI_COL2_VALUE, courseGradeStats.getRank());
		        }
	        }
	        else
	        {
	        	GWT.log("Course stats is null", null);
	        }
	        
        }
        studentInformationPanel.show();
	}

	
	private BaseModel populateGradeInfoRow(int row, ItemModel item, StudentModel learner, StatisticsModel stats, CategoryType categoryType) {
		String itemId = item.getIdentifier();
		Object value = learner.get(itemId);
		String commentFlag = new StringBuilder().append(itemId).append(StudentModel.COMMENT_TEXT_FLAG).toString();
		String comment = learner.get(commentFlag);
		String excusedFlag = new StringBuilder().append(itemId).append(StudentModel.DROP_FLAG).toString();
		
		String mean = (stats == null ? "" : stats.getMean());
		String stdDev = (stats == null ? "" : stats.getStandardDeviation()); 
		String median = (stats == null ? "" : stats.getMedian());
		String mode = (stats == null ? "" : stats.getMode()); 
		String rank = (stats == null ? "" : stats.getRank());
		
		if (comment == null)
			comment = "N/A";
		
		boolean isExcused = DataTypeConversionUtil.checkBoolean((Boolean)learner.get(excusedFlag));
		boolean isIncluded = DataTypeConversionUtil.checkBoolean((Boolean)item.getIncluded());
		
		BaseModel model = new BaseModel();
		
		StringBuilder id = new StringBuilder();
		
		switch (categoryType) {
		case WEIGHTED_CATEGORIES:
		case SIMPLE_CATEGORIES:
			model.set(Key.CATEGORY_NAME.name(), item.getCategoryName());
			id.append(item.getCategoryId()).append(":");
		default:
			model.set(Key.ITEM_NAME.name(), item.getName());
			model.set(Key.COMMENT.name(), comment);
			id.append(itemId);
		}
		
		model.set(Key.ID.name(), id.toString());
		
        StringBuilder resultBuilder = new StringBuilder();
        if (value == null)
        	resultBuilder.append("-");
        else {
        	
        	switch (selectedGradebook.getGradebookItemModel().getGradeType()) {
        	case POINTS:
        		resultBuilder.append(NumberFormat.getDecimalFormat().format(((Double)value).doubleValue()));
        		
        		if (item.getPoints() != null)
        			resultBuilder.append(" out of ").append(NumberFormat.getDecimalFormat().format(item.getPoints().doubleValue())).append(" points");
        		
        		break;
        	case PERCENTAGES:
        		resultBuilder.append(NumberFormat.getDecimalFormat().format(((Double)value).doubleValue()))
        			.append("%");
        		
        		break;
        	case LETTERS:
        		resultBuilder.append(value);
        		break;
        	}
        	
        	
        }
        
        if (!isIncluded) 
        	resultBuilder.append(" (not included in grade)");
        
        if (isExcused)
        	resultBuilder.append(" (excused)");
        
        model.set(Key.GRADE.name(), resultBuilder.toString());
        
        if (stats != null) {
        	model.set(Key.MEAN.name(), mean);
        	model.set(Key.STDV.name(), stdDev);
        	model.set(Key.MEDI.name(), median);
        	model.set(Key.MODE.name(), mode);
        	model.set(Key.RANK.name(), rank);
        }
        
        
        model.set(Key.ORDER.name(), String.valueOf(row));
     
        return model;
	}
	
	// So for stats, we'll have the following columns: 
	// grade | Mean | Std Deviation | Median | Mode | Comment 

	private void setGradeInfoTable(GradebookModel selectedGradebook, StudentModel learner, List<StatisticsModel> statsList) {		
		// To force a refresh, let's first hide the owning panel
		gradeInformationPanel.hide();
		
		ItemModel gradebookItemModel = selectedGradebook.getGradebookItemModel();
		CategoryType categoryType = gradebookItemModel.getCategoryType();
		
		store.removeAll();
		
		boolean isDisplayReleasedItems = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getReleaseItems());
		if (isDisplayReleasedItems) {
			boolean isNothingToDisplay = true;
			int row=0;
			
			ArrayList<BaseModel> models = new ArrayList<BaseModel>();
			int childCount = gradebookItemModel.getChildCount();
			if (childCount > 0) {
				for (int i=0;i<childCount;i++) {
					ItemModel child = gradebookItemModel.getChild(i);
					switch (child.getItemType()) {
					case CATEGORY:
						int itemCount = child.getChildCount();
						if (itemCount > 0) {
							for (int j=0;j<itemCount;j++) {
								ItemModel item = child.getChild(j);
								if (DataTypeConversionUtil.checkBoolean(item.getReleased())) {
									StatisticsModel stats = null; 
									stats = getStatsModelForItem(item.getIdentifier(), statsList); 
				
									models.add(populateGradeInfoRow(i*1000 + j + 10000, item, learner, stats, categoryType));
									isNothingToDisplay = false;
									row++;
								} 
							}
						}
						break;
					case ITEM:
						StatisticsModel stats = null; 
						stats = getStatsModelForItem(child.getIdentifier(), statsList); 

						if (DataTypeConversionUtil.checkBoolean(child.getReleased())) {
							models.add(populateGradeInfoRow(row, child, learner, stats, categoryType));
							isNothingToDisplay = false;
							row++;
						}
						break;
					}
				}
				
				store.add(models);
				store.groupBy(Key.CATEGORY_NAME.name());
			}
			
			if (isNothingToDisplay) {
				cardLayout.setActiveItem(textPanel);
				I18nConstants i18n = Registry.get(AppConstants.I18N);
				textNotification.setHtml(i18n.notifyNoReleasedItems());
			} else {
				cardLayout.setActiveItem(gradeInformationPanel);
			}
			
		} else {
			cardLayout.setActiveItem(textPanel);
			I18nConstants i18n = Registry.get(AppConstants.I18N);
			textNotification.setHtml(i18n.notifyNotDisplayingReleasedItems());
		}

        gradeInformationPanel.show();
	}
	
	private StatisticsModel getStatsModelForItem(String id, List<StatisticsModel> statsList) {
		int idx = -1; 
		
		StatisticsModel key = new StatisticsModel();
		key.setAssignmentId(id); 
		idx = Collections.binarySearch(statsList, key);
		
		if (idx >= 0)
		{
			return statsList.get(idx); 
		}
		
		return null;
	}

	public boolean isStudentView() {
		return isStudentView;
	}

	public void setStudentView(boolean isStudentView) {
		this.isStudentView = isStudentView;
	}

	public StudentModel getStudentModel() {
		return learnerGradeRecordCollection;
	}
}
