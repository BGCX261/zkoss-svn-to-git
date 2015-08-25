package org.zerokode.designer.ui;

import org.zkoss.zul.Groupbox;


/**
 * Implements a designer Groupbox component
 * @author chris.spiliotopoulos
 *
 */
public class DesignerGroupbox extends Groupbox
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8658938516969890172L;
	/**
	 * The parent designer instance 
	 */
	protected Designer _designer = null;
	
	
	// Getters / Setters
	public Designer getDesigner() { return _designer; }
	
	/**
	 * Constructor that sets the parent 
	 * designer window 
	 * @param designer
	 */
	DesignerGroupbox(Designer designer)
	{
		_designer = designer;
	}
	
}
