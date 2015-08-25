package org.zerokode.designer.ui;

import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.IDisposable;
import org.zerokode.designer.model.ModelWriter;
import org.zerokode.designer.model.ZUMLModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;


/**
 * Implements a modal dialog that gets
 * displayed each time a model is about to 
 * be saved into a file.
 * @author chris.spiliotopoulos
 *
 */
public class SaveModelDialog extends Window
						     implements EventListener, 
						     		    IDisposable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6993444195214265984L;

	/**
	 * The current designer instance 
	 */
	private ZUMLModel _model = null;
	
	/**
	 * The name for the page to be saved 
	 */
	private String _sPageName = "";
	
	/**
	 * 1-argument constructor
	 * @param designer The current designer instance
	 */
	public SaveModelDialog(ZUMLModel model)
	{
		_model = model;
	}
	
	/**
	 * Create the dialog
	 */
	public void onCreate()
	{
		// set the window properties
		setId("dlgSaveModel");
		setTitle("Save Model To File");
		setWidth("400px");
		setBorder("normal");
		setClosable(true);
		
		// create a Label
		Label lbl = new Label("Enter a name for the page:");
		appendChild(lbl);
		
		// create a Textbox to thold the page name
		Textbox text = new Textbox();
		text.setId("txtPageName");
		text.setWidth("100%");
		
		// create the 'Save' button
		Button btnSave = new Button("Save");
		btnSave.setId("btnSave");
		btnSave.addEventListener("onClick", this);
		
		// Vertical layout
		Vbox vbox = new Vbox();
		vbox.setWidth("99%");
		vbox.appendChild(lbl);
		vbox.appendChild(text);
		vbox.appendChild(btnSave);
		appendChild(vbox);
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
	 */
	public void onEvent(Event evt)
	{
		/*** Save Page Definition ***/
		if (evt.getTarget().getId().equals("btnSave"))
		{
			// get the entered page name
			Textbox txtPageName = (Textbox) getFellow("txtPageName");
			_sPageName = txtPageName.getValue();
			savePageToFile(_sPageName);
		}
		
		// close the dialog
		onClose();
	}

	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#isAsap()
	 */
	public boolean isAsap()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see com.potix.zul.html.Window#onClose()
	 */
	public void onClose()
	{
		// end the modal state first
		this.doEmbedded();
		
		// close the window
		super.onClose();
	}

	/**
	 * Saves the page definition to the specified
	 * file.
	 * @param sFilename The full filename
	 */
	private void savePageToFile(String sPageName) 
	{
		if (_model == null) 
			return;

		try
        {
			if (StringUtils.isEmpty(sPageName))
			{
				// display an error message and exit
				Messagebox.show("You have not specified a name for the page", 
								"Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}
			
			// get the application object
            WebApp app = Executions.getCurrent().getDesktop().getWebApp();
            
            // remove any '.zul' extensions from the page name
            sPageName = StringUtils.removeEnd(sPageName, ".zul");
            
			// construct the full path filename
            String sFilename = app.getRealPath("/saved_designs/" + sPageName + ".zul");
			
    		// print the ZUML Document structure to the stream 
    		ModelWriter.writeModelToFile(_model, sFilename, ModelWriter.MODE_NORMAL);

			// if the model was successfuly saved to the specified file, 
            // let the user know about it
			Messagebox.show("Model was successfuly saved to file [" + sFilename + "]", 
						    "Success", Messagebox.OK, Messagebox.INFORMATION);
        }
        catch (Exception e)
        {
			try
			{
	        	// display an error message and exit
				Messagebox.show("Error writing to file [" + e.getMessage() + "]", 
								"Error", Messagebox.OK, Messagebox.ERROR);
			}
			catch (InterruptedException ie)
			{
			}
        }
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// clean up
		if (_model != null)
		{
			_model.dispose();
			_model = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable
	{
		try
		{
			// clean up
			dispose();
		}
		catch (Exception e)	{ }
		finally
		{
			super.finalize();
		}
	}
}
