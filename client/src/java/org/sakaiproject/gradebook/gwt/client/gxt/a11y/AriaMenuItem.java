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

package org.sakaiproject.gradebook.gwt.client.gxt.a11y;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Accessibility;

public class AriaMenuItem extends MenuItem {

	public AriaMenuItem() {
		super();
	}
	
	public AriaMenuItem(String text) {
		super(text);
	}
	
	public AriaMenuItem(String text, SelectionListener listener) {
		super(text, listener);
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		Accessibility.setRole(el().dom, Accessibility.ROLE_MENUITEM);
	}
	
}
