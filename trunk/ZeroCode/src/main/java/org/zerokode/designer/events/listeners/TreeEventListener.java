package org.zerokode.designer.events.listeners;

import org.zerokode.designer.model.ComponentFactory;
import org.zerokode.designer.model.rules.engine.RulesEngine;
import org.zerokode.designer.model.rules.engine.RulesResult;
import org.zerokode.designer.ui.Designer;
import org.zerokode.designer.ui.DesignerCanvas;
import org.zerokode.designer.ui.DesignerTree;
import org.zerokode.designer.ui.PropertiesWindow;
import org.zerokode.designer.ui.DesignerTree.MoveItemDialog;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Image;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;




/**
 * Listens for designer Tree events
 * @author chris.spiliotopoulos
 *
 */
public class TreeEventListener implements EventListener
{

	/**
	 * Reference to the designer canvas
	 */
	protected DesignerCanvas _canvas = null;
	
	/**
	 * Reference to the designer Tree
	 */
	protected DesignerTree _tree = null;
	
	/**
	 * The selected Canvas component 
	 */
	protected Component _selectedComponent = null;
	
	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
	 */
	public void onEvent(Event evt)
	{
		try
		{
			// get the designer Tree component
			_tree = (DesignerTree) evt.getTarget().getFellow("designerTree");
			
			if (_tree == null)
				return;
			
			// get the designer canvas
			_canvas = _tree.getDesigner().getCanvas();
			
			// get the selected component from the Canvas
			_selectedComponent = _tree.getSelectedComponent();

			// if this is a drag-and-drop event, add 
			// the component to the model and return
			if (evt instanceof DropEvent)
			{
				// downcast to DropEvent
				DropEvent evtDrop = (DropEvent) evt;
				
				// check the type of dragged item
				
				/*** Dragged: Image --> Target: Tree ***/
				if (evtDrop.getDragged() instanceof Image)
				{
					// insert the selected toolkit Component
					// into the canvas model
					addComponentToModel(evtDrop);
					return;
				}
				
				/*** Dragged: Treerow --> Target: Treerow ***/
				if (evtDrop.getDragged() instanceof Treerow)
				{
					// move the dragged component to the new position
					moveComponent(evtDrop);
					return;
				}
			}
			
			/*** Refresh Canvas ***/
			if (evt.getTarget().getId().equals("btnRefresh"))
			{
				// refresh the designer canvas
				refreshCanvas();
			}
			
			// clean up some memory now
			Designer.getCurrent().cleanUpMemory();
		}
		catch (Exception e)
		{
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
	 * Displays the properties of the selected Tree component
	 * on a modal window
	 * @param selectedComponent
	 */
	protected void displayComponentProperties(Component selectedComponent, 
											  int nViewMode)
	{
		try
		{
			if (selectedComponent == null)
				return;
			
			// create a new Properties window and display it as modal
			PropertiesWindow wndProps = new PropertiesWindow(_canvas.getDesigner(), 
					                                         (AbstractComponent) selectedComponent, 
					                                         nViewMode);			
			wndProps.onCreate();
			
			// append the Properties window to the designer page
			_canvas.getDesigner().appendChild(wndProps);
			
			// display it as popup
			wndProps.doModal();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * Adds a component to the canvas model, as soon 
	 * as the user has dragged-and-dropped a component
	 * from the toolkit onto the Tree.
	 * @param evt The Drop event object
	 */
	protected void addComponentToModel(DropEvent evtDrop)
	{
		Component newComponent 	= null;
		Treeitem itemTarget 	= null;
		Component cmpDragged 	= null; 
		Component cmpTarget 	= null; 
		
		try
		{
			// get the dragged and target components
			cmpDragged = evtDrop.getDragged();
			itemTarget = (Treeitem) evtDrop.getTarget().getParent();

			// get the canvas Components that correspond to the 
			// dragged and target Treeitems respectively
			cmpTarget = _tree.getCorrespondingCanvasComponent(itemTarget);
			
			// if either component is null or that dragged and target
			// are the same object, exit
			if ((cmpTarget == null) || (cmpDragged == null) || 
				(cmpTarget == cmpDragged))
				return;
			
			// create a new component instance of this class
			newComponent = ComponentFactory.createComponent(cmpDragged.getId());		
		
			if (newComponent == null)
				return;
		
			// check if the dragged component allows children
			if (! cmpTarget.isChildable())
			{	
				// display the exception string in an error box
				Messagebox.show("Target component does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}
			
			// add the new component as a child
			// to the selected one
			cmpTarget.appendChild(newComponent);
			cmpTarget.invalidate(); // avoid some side effect. add By Jumper
			// apply the post creation rules of the component
			RulesResult result = RulesEngine.applyRules(newComponent, RulesEngine.CREATION_RULES);

			// clean up
			result = null;
		}
		catch (Exception e)
		{
			try
			{
				if (newComponent != null)
					cmpTarget.removeChild(newComponent);
				
				// display the exception string in an error box
				Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}

			return;
		}
		
		// if the operation was successful, 
		// synchronize the Tree with the model
		_tree.getDesigner().getSynchronizer().setTargetTreeitem(itemTarget.getId());
		_tree.getDesigner().getSynchronizer().synchronizeTreeWithCanvas(_canvas);
	}
	
	/**
	 * Adds a component to the canvas model, as soon 
	 * as the user has dragged-and-dropped a component
	 * from the toolkit onto the Tree.
	 * @param evt The Drop event object
	 */
	protected void moveComponent(DropEvent evtDrop)
	{
		Component cmpDragged 		= null;
		Component cmpParent		 	= null;
		Component cmpTarget 		= null;
		
		try
		{
			if (_tree == null)
				return;
			
			// get the dragged and target Treeitems
			Treeitem itemDragged = (Treeitem) evtDrop.getDragged().getParent();
			Treeitem itemTarget = (Treeitem) evtDrop.getTarget().getParent();

			// get the canvas Components that correspond to the 
			// dragged and target Treeitems respectively
			cmpDragged = _tree.getCorrespondingCanvasComponent(itemDragged);
			cmpTarget = _tree.getCorrespondingCanvasComponent(itemTarget);
			
			
			// if either component is null or that dragged and target
			// are the same object, exit
			if ((cmpTarget == null) || (cmpDragged == null) || 
				(cmpTarget == cmpDragged))
				return;
			
			// display the 'Move Component' dialog
			DesignerTree.MoveItemDialog wndMove = _tree.new MoveItemDialog(_tree);
			wndMove.onCreate();
			
			// display it as modal
			wndMove.doModal();
			
			// get the selected move type
			int nMoveType = wndMove.getMoveType();
			
			// if the move was cancelled, exit
			if (nMoveType == MoveItemDialog.DD_NONE)
				return;
			
			// perform a position change between the dragged 
			// and target components, based on the move type
			// that the user has selected
			
			if (nMoveType == MoveItemDialog.DD_AS_CHILD)
			{
				/*** Append as child ***/
				
				// append only if the target component allows children
				if (! cmpTarget.isChildable())
				{	
					// display the exception string in an error box
					Messagebox.show("Target component does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// check if the dragged component is an ancestor of
				// the target one
				if (Components.isAncestor(cmpDragged, cmpTarget))
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component is an ancestor of the target component...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// detach the dragged component from its parent
				// and append it as a child to the target one
				cmpDragged.detach();
				cmpTarget.appendChild(cmpDragged);
			}
			else if (nMoveType == MoveItemDialog.DD_AS_PARENT)
			{
				/*** Set as parent ***/
				
				// check if the dragged component allows children
				if (! cmpDragged.isChildable())
				{	
					// display the exception string in an error box
					Messagebox.show("Dragged component does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// get the parent of the target component
				cmpParent = cmpTarget.getParent();
				
				if (! cmpParent.isChildable())
				{	
					// display the exception string in an error box
					Messagebox.show("Target component's parent does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}	
				
				// detach the dragged and target components				
				cmpDragged.detach();
				cmpTarget.detach();
				
				// append the dragged component to the target's parent
				cmpParent.appendChild(cmpDragged);
				
				// append the target component to the dragged one
				cmpDragged.appendChild(cmpTarget);	
			}
			else if (nMoveType == MoveItemDialog.DD_BEFORE)
			{
				/*** Insert Before ***/
				
				// check if the dragged component is an ancestor of
				// the target one
				if (Components.isAncestor(cmpDragged, cmpTarget))
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component is an ancestor of the target component...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// check if the user has tried to to insert the
				// dragged element before the root canvas component
				if (_canvas.getRootComponent() == cmpTarget)
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component cannot be inserted before the root window...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// detach the dragged element and insert it before
				// the target element
				cmpDragged.detach();
				cmpTarget.getParent().insertBefore(cmpDragged, cmpTarget);
			}
		}
		catch (Exception e)
		{
			try
			{
				// display the exception string in an error box
				Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			
			return;
		}
		
		// if the operation was successful, 
		// synchronize the Tree with the model
		_tree.getDesigner().getSynchronizer().synchronizeTreeWithCanvas(_canvas);
	}
	
	/**
	 * Refreshes the designer canvas with the
	 * current component model 
	 */
	private void refreshCanvas()
	{
		if (_canvas == null)
			return;
		
		// refresh the canvas
		_canvas.refreshCanvas();
	}
}
