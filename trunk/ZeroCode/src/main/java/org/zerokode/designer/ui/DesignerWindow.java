package org.zerokode.designer.ui;

import org.zkoss.zul.Window;




/**
 * Class the implements a designer's window
 * component.
 * @author chris.spiliotopoulos
 *
 */
public abstract class DesignerWindow extends Window
{
	/**
	 * The parent designer instance 
	 */
	protected Designer _designer = null;
	
	
	/**
	 * Constructor that sets the parent 
	 * designer window 
	 * @param designer
	 */
	DesignerWindow(Designer designer)
	{
		_designer = designer;
	}
	
	// Getters / Setters
	public Designer getDesigner() { return _designer; }

}
