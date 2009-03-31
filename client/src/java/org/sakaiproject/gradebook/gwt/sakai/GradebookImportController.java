package org.sakaiproject.gradebook.gwt.sakai;

import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.gradebook.gwt.client.GradebookToolFacade;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportFile;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility.Delimiter;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class GradebookImportController extends SimpleFormController {
	
	private static final Log log = LogFactory.getLog(GradebookImportController.class);
	
	private GradebookToolFacade delegateFacade;
	private UserDirectoryService userService;
	
	/*protected ModelAndView showForm(
			HttpServletRequest request, HttpServletResponse response, BindException errors)
			throws Exception {

		return new ModelAndView(new GradebookExportView(delegateFacade));
	}*/
	
	protected ModelAndView onSubmit(HttpServletRequest request,
	        HttpServletResponse response,
	        Object command, BindException errors) throws Exception {

	         // cast the bean
	        //FileUploadBean bean = (FileUploadBean) command;

			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		
			String gradebookUid = multipartRequest.getParameter("gradebookUid");
			
			EnumSet<Delimiter> delimiterSet = EnumSet.noneOf(Delimiter.class);
			for (Enumeration<String> paramNames = multipartRequest.getParameterNames();paramNames.hasMoreElements();) {
				String name = paramNames.nextElement();
				if (name.equals("delimiter:comma")) 
        			delimiterSet.add(Delimiter.COMMA);
        		else if (name.equals("delimiter:tab")) 
        			delimiterSet.add(Delimiter.TAB);
        		else if (name.equals("delimiter:space"))
        			delimiterSet.add(Delimiter.SPACE);
        		else if (name.equals("delimiter:colon"))
        			delimiterSet.add(Delimiter.COLON);
			}
			
			for (Iterator<String> fileNameIterator = multipartRequest.getFileNames();fileNameIterator.hasNext();) {
				String fileName = fileNameIterator.next();
				MultipartFile file = multipartRequest.getFile(fileName);
				
				//InputStream fileInputStream = file.getInputStream();
				byte[] bytes = file.getBytes();
				if (bytes != null) {
					String content = new String(bytes);
					
					ImportFile importFile = ImportExportUtility.parseImportX(delegateFacade, userService, gradebookUid, content, delimiterSet);
			        
		        	PrintWriter writer = response.getWriter();
		        	response.setContentType("text/html");
		    		
		        	XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
		        	writer.write(xstream.toXML(importFile)); 
		        	writer.flush();
		        	writer.close();
				}
			}

			return null; //super.onSubmit(request, response, command, errors);
	    }

	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		// to actually be able to convert Multipart instance to byte[]
		// we have to register a custom editor
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		// now Spring knows how to handle multipart object and convert them
	}

	
	public GradebookToolFacade getDelegateFacade() {
		return delegateFacade;
	}

	public void setDelegateFacade(GradebookToolFacade delegateFacade) {
		this.delegateFacade = delegateFacade;
	}

	public UserDirectoryService getUserService() {
		return userService;
	}

	public void setUserService(UserDirectoryService userService) {
		this.userService = userService;
	}

}