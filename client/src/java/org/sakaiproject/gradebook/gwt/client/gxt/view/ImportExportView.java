package org.sakaiproject.gradebook.gwt.client.gxt.view;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ImportPanel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class ImportExportView extends View {

	private ImportPanel importDialog;
	private Frame downloadFileFrame;
	
	public ImportExportView(Controller controller) {
		super(controller);
	}

	@Override
	protected void handleEvent(AppEvent<?> event) {
		
		switch (GradebookEvents.getEvent(event.type).getEventKey()) {
		case START_IMPORT:
			importDialog = new ImportPanel();
			//importDialog.setSize(XDOM.getViewportSize().width - 50, 500);
			//importDialog.show();
			//importDialog.center();
			break;
		case START_EXPORT:
			GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
			String uri = GWT.getModuleBaseURL() + "/exportGradebook.csv?gradebookUid=" + selectedGradebook.getGradebookUid();
			
			if (downloadFileFrame == null) {
				downloadFileFrame = new Frame(uri);
				RootPanel.get().add(downloadFileFrame);
			} else {
				downloadFileFrame.setUrl(uri);
			}
			break;
		}
	}

	public ImportPanel getImportDialog() {
		return importDialog;
	}

}
