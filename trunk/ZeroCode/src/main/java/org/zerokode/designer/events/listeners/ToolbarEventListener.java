package org.zerokode.designer.events.listeners;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.zerokode.designer.model.ModelWriter;
import org.zerokode.designer.model.ZUMLModel;
import org.zerokode.designer.ui.Designer;
import org.zerokode.designer.ui.DesignerCanvas;
import org.zerokode.designer.ui.InfoDialog;
import org.zerokode.designer.ui.SaveModelDialog;
import org.zkoss.util.media.Media;
import org.zkoss.zhtml.Fileupload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;



/**
 * Listens for designer Toolbar events 
 * @author chris.spiliotopoulos
 *
 */
public class ToolbarEventListener	implements
									EventListener
{
	
	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
	 */
	public void onEvent(Event evt)
	{
		try
		{
			// process the event depending on the source
			
			/*** Load Page Definition ***/
			if (evt.getTarget().getId().equals("fiLoadPageDefinition"))
				loadPageDefinition(evt);
		
			/*** Save Page Definition ***/
			if (evt.getTarget().getId().equals("fiSavePageDefinition"))
			{
				savePageDefinition(evt);
			}	
			
			/*** Clear Canvas ***/
			if (evt.getTarget().getId().equals("fiClearCanvas"))
				clearCanvas(evt);
			
			/*** View Page ***/
			if (evt.getTarget().getId().equals("fiViewPage"))
				viewPage(evt);
			
			/*** Info ***/
			if (evt.getTarget().getId().equals("fiInfo"))
				showInfo(evt);
			
			// clean up some memory now
			Designer designer = Designer.getCurrent();
			
			if (designer != null)
				designer.cleanUpMemory();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#isAsap()
	 */
	public boolean isAsap()
	{
		return true;
	}

	/**
	 * Loads a page definition from a '*.zul'
	 * stored in the filesystem and displays
	 * the page on the designer canvas
	 */
	protected void loadPageDefinition(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			// display the Fileupload dialog
			Media media = Fileupload.get("Select a '*.zul' file from the explorer and it will be displayed on the canvas", "Load page definition");

			if (media == null)
				return;
			
			// get the stream of the selected '*.zul' file
			InputStream stream = media.getStreamData();
			InputStreamReader reader = new InputStreamReader(stream);
			
			// load the model onto the Canvas
			wndCanvas.loadModelFromStream(reader, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts the model currently displayed
	 * on the canvas into the corresponding 
	 * ZUML representation and asks the user
	 * to save the '*.ZUL' file into a specified
	 * location.
	 */
	protected void savePageDefinition(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			// get the ZUML representation of the model
			ZUMLModel model = wndCanvas.getZUMLRepresentation();
			
			if (model == null)
				return;
			
			/*** save the ZUML definition into a file ***/
			/***										   ***/
			
			// create a new Properties window and display it as modal
			SaveModelDialog dlgSave = new SaveModelDialog(model);			
			dlgSave.onCreate();
			wndCanvas.getDesigner().appendChild(dlgSave);
			dlgSave.doModal();
			
			/***										   ***/
			/*** save the ZUML definition to a file ***/
			
			// clean up
			model.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Converts the model currently displayed
	 * on the canvas into the corresponding 
	 * ZUML representation and asks the user
	 * to save the '*.ZUL' file into a specified
	 * location.
	 */
	protected void savePageDefinitionTest(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			Component root = (Component) wndCanvas.getChildren().get(0);
			
			if (root == null)
				return;
/*			
			try
			{
				// write the component model to a file
				ZUMLWriter.writeToFile(root, "c:/test/output.zul");				
				
			}
			catch (ZKBException e)
			{
				e.printStackTrace();
				return;
			}
*/			
			/***										   ***/
			/*** save the ZUML definition to a file ***/
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Deletes the current component model
	 * from the canvas.
	 * @param evt
	 */
	protected void clearCanvas(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			// clear the canvas
			wndCanvas.clearCanvas();
			
			// delete the old filenames held by the designer object
			wndCanvas.getDesigner().setSourceFilename("");
			wndCanvas.getDesigner().setTargetFilename("");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
		
	/**
	 * Deletes the current component model
	 * from the canvas.
	 * @param evt
	 */
	protected void viewPage(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			// get the ZUML representation of the model
			ZUMLModel model = wndCanvas.getZUMLRepresentation();
			
			if (model == null)
				return;
			
			// get the real path of the 'Preview.zul' file
            WebApp app = wndCanvas.getDesigner().getWebApp();
            String sFilename = app.getRealPath("/Preview.zul");
            
            try
            {
        		// print the ZUML Document to the 'Preview.zul' file 
        		ModelWriter.writeModelToFile(model, sFilename, ModelWriter.MODE_SILENT);
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	return;
            }
            finally
            {
            	// clean up
            	model.dispose();
            }
			
			// send a redirect to the 'Preview.zul' page
			Executions.getCurrent().sendRedirect("/Preview.zul", "_blank");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays the Info dialog
	 */
	protected void showInfo(Event evt)
	{
		try
		{
			// first get the Canvas window
			DesignerCanvas wndCanvas = (DesignerCanvas) evt.getTarget().getFellow("designerCanvas");
			
			if (wndCanvas == null)
				return;
			
			// create the InfoDialog and display it as modal
			InfoDialog dlgInfo = new InfoDialog();			
			dlgInfo.onCreate();
			wndCanvas.getDesigner().appendChild(dlgInfo);
			dlgInfo.doModal();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
