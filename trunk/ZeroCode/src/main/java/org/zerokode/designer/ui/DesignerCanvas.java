package org.zerokode.designer.ui;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.IDisposable;
import org.zerokode.designer.model.ComponentFactory;
import org.zerokode.designer.model.ZUMLModel;
import org.zkoss.idom.Document;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Treeitem;




/**
 * Class that utilizes the Widget View of the
 * designer, where the user selects widgets
 * and places them on the canvas.
 * @author chris.spiliotopoulos
 *
 */
public class DesignerCanvas extends DesignerWindow
							implements IDisposable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1247064797503789963L;

	/**
	 *  
	 */
	protected Component _locatedComponent = null;
	
	/**
	 * Flag that indicates if the canvas model has differences
	 * compared to the last saved instance 
	 */
	private boolean _bIsCanvasDirty = false;
	
	/**
	 * @param designer
	 */
	DesignerCanvas(Designer designer)
	{
		super(designer);
	}

	// Getters / Setters
	public boolean isCanvasDirty() { return _bIsCanvasDirty; } 
	
	/**
	 *  
	 * @param main
	 */
	public void onCreate() 
	{ 
		// create the Canvas window
		setId("designerCanvas");
		setTitle("Canvas");
		setWidth("100%");
		setHeight("500px");
		setBorder("normal");
	}
	
	/**
	 * Returns the top most element
	 * in the drawing canvas.
	 */
	public Component getRootComponent()
	{
		// return the first child component of the canvas
		if ((getChildren() != null) && (getChildren().size() > 0)) 
			return (Component) getChildren().get(0);
		else
			return null;
	}
	
	/**
	 * Loads a page model from a file stream
	 * @param modelReader The stream Reader
	 * @param bSynchronizeTree if <b>true</b> the tree
	 * is synchronized with the current canvas model
	 */
	public void loadModelFromStream(Reader modelReader, 
			  						boolean bSynchronizeTree)
	{
		if (modelReader == null)
			return;
		
		try
		{
			// remove all components from the canvas window
			Components.removeAllChildren(this);
			
			// create the component model described in the '*.zul' file
			// onto the designer Canvas
			Component root = Executions.createComponentsDirectly(modelReader, null, this, null);
			
			// synchronize the Tree with the model
			if (bSynchronizeTree)
				getDesigner().getSynchronizer().synchronizeTreeWithCanvas(this);
			
			// turn the 'DIRTY' flag off
			_bIsCanvasDirty = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a page model from an iDOM Document
	 * @param modelReader The Document object that 
	 * holds the model representation
	 * @param bSynchronizeTree if <b>true</b> the tree
	 * is synchronized with the current canvas model
	 */
	public void loadModelFromDocument(Document modelDocument, 
									  boolean bSynchronizeTree)
	{
		if (modelDocument == null)
			return;
		
		try
		{
			// remove all components from the canvas window
			Components.removeAllChildren(this);
			
			// create the component model described in the '*.zul' file
			// onto the designer Canvas
			//Executions.createComponentsDirectly(modelDocument, null, this, null);
			Executions.createComponentsDirectly(modelDocument, null, this, null);			
			
			// synchronize the Tree with the model
			if (bSynchronizeTree)
				getDesigner().getSynchronizer().synchronizeTreeWithCanvas(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the input Component model that is
	 * currently displayed on the designer canvas
	 * and displays each discreet UI component as 
	 * a Tree item on the designer Tree.
	 * @param wndCanvas The designer Canvas
	 * @param tree The designer Tree
	 */
	public Component getCanvasComponent(String sId)
	{
		if (StringUtils.isEmpty(sId))
			return null;
		
		_locatedComponent = null;
		
		locateComponent(this.getRoot(), sId);
		
		return _locatedComponent;
	}
	
	/**
	 * Locates a canvas component, given a Treeitem.
	 * @param item A Treeitem from the model tree
	 * @return The corresponding canvas component
	 */
	public Component getCanvasComponent(Treeitem item)
	{
		if (item == null)
			return null;
		
		// strip the Treeitem's Id from the 'id_' prefix
		// in order to the the corresponding canvas Id
		String sId = item.getId();
		sId = StringUtils.removeStart(sId, "id_");
		
		// now call the right method to do the job
		return getCanvasComponent(sId);
	}
	
	/**
	 * Parses the input Component model that is
	 * currently displayed on the designer canvas
	 * and displays each discreet UI component as 
	 * a Tree item on the designer Tree.
	 * @param wndCanvas The designer Canvas
	 * @param tree The designer Tree
	 */
	protected Component locateComponent(Component canvasComponent, 
										String sId)
	{
		if (_locatedComponent != null)
			return _locatedComponent;
		
		if (canvasComponent == null)
			return null;
		
		// get the component's children
		List listChildren = canvasComponent.getChildren();

		if ((listChildren == null) || (listChildren.size() == 0))
			return null;
		
		// loop through all the component's children
		Iterator iter = listChildren.iterator();
		while (iter.hasNext())
		{
			// get the next component in the list
			Component child = (Component) iter.next();
			
			if (child == null)
				continue;

			if (child.getUuid().equals(sId))
			{
				_locatedComponent = child;
				break;
			}
			
			// parse the model of the child
			locateComponent(child, sId);
		}
		
		return null;
	}
	
	/**
	 * Deletes the current component model
	 * from the canvas.
	 * @param evt
	 */
	public void clearCanvas()
	{
		try
		{
			// dispose the component's resources first
			ComponentFactory.disposeComponent(this);
			
			// detach all the components from the canvas
			Components.removeAllChildren(this);			
			
			// clear the Tree
			getDesigner().getTree().clearTree();
			
			// turn the 'DIRTY' flag on
			_bIsCanvasDirty = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts the model into its ZUML
	 * equivalent using a custom convertor. 
	 * @return The convertor object that holds
	 * the ZUML model representation
	 */
	public ZUMLModel getZUMLRepresentation()
	{
		if (getChildren().get(0) == null)
			return null;
		
		// create a model-to-ZUML convertor instance
		ZUMLModel model = new ZUMLModel((Component) getChildren().get(0));
		
		return model;
	}
	
	/**
	 * Refreshes the canvas by reloading the
	 * active model and synchronizing the tree.
	 */
	public void refreshCanvas()
	{
		// convert the current model into a ZUML Document
		// create a model-to-ZUML convertor instance
		ZUMLModel model = new ZUMLModel((Component) getChildren().get(0));
		
		// reload the model onto the canvas
		loadModelFromDocument(model.getZUMLDocument(), true);
		
		// clean up
		model.dispose();
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// dispose the component
//		ComponentFactory.disposeComponent(_locatedComponent);
//		ComponentFactory.disposeComponent((Component) this.getChildren().get(0));
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