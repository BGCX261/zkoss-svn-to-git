package org.zerokode.designer.ui;

import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.events.listeners.TreeEventListener;
import org.zerokode.designer.model.ComponentFactory;
import org.zerokode.designer.model.rules.engine.RulesEngine;
import org.zerokode.designer.model.rules.engine.exceptions.DisplayableRulesException;
import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;



/**
 * UI module that displays the Treeview component
 * that shows in real time all the widgets contained
 * in a specific designer page.
 * @author chris.spiliotopoulos
 *
 */
public class DesignerTree extends DesignerGroupbox
{	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6751609056828648667L;
	/**
	 * The tree widget 
	 */
	protected Tree _tree = null;

	/**
	 * Reference to the 'Move Component' dialog 
	 */
	protected MoveItemDialog _wndMove = null;
	
	/**
	 * The component dragged and dropped onto the Tree
	 */
	protected Component _cmpDragged = null;
	
	/**
	 * The target component of the drag-and-drop operation 
	 */
	protected Component _cmpTarget = null;
	
	/**
	 * The popup menu that get's displayed when the 
	 * user right-clicks on the tree
	 */
	protected TreeitemContextMenu _menu = null;
	
	/**
	 * The Id of the element that the user has selected
	 * to copy-and-paste
	 */
	protected String _sElementToPasteId = "";
	
	/**
	 * @param designer
	 */
	DesignerTree(Designer designer)
	{
		super(designer);
	}
	
	// Getters / Setters
	public Tree getTree() { return _tree; }
	public String getElementToPasteId() { return _sElementToPasteId; }
	public void setElementToPasteId(String sId) { _sElementToPasteId = sId; }
	
	/**
	 * Creates the Tree view window
	 * @param main
	 */
	public void onCreate() 
	{ 
		// create the Tree view window
		setId("designerTree");
		setWidth("300px");
		setMold("3d");
		setTooltiptext("Real-time representation of the canvas model.");
		
		// create the Caption
		Caption caption = new Caption();
		caption.setLabel("Model Structure");
		appendChild(caption);
		
		// create the Tree component
		_tree = new Tree();
		_tree.setWidth("400px");
		_tree.setTooltiptext("Re-order canvas elements by dragging-and-dropping the corresponding tree items. Right-click items for context menu.");
		appendChild(_tree);
		
		// initialize the Tree
		clearTree();
		
		// create the 'Refresh Canvas' button
		Button btnRefresh = new Button("Refresh Canvas");
		Image img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/buttons/refresh.png");
		btnRefresh.setImageContent(img.getContent());
		btnRefresh.setId("btnRefresh");
		btnRefresh.addEventListener("onClick", new TreeEventListener());
		btnRefresh.setOrient("vertical");
		appendChild(btnRefresh);
						
		// create the popup menu
		_menu = new TreeitemContextMenu();
		_menu.onCreate();
		appendChild(_menu);
		setContext("treeitemPopup");
	}
	
	/**
	 * Retrieves the selected Treeitem and 
	 * returns the corresponding Component
	 * from the Canvas.
	 * @return The corresponding canvas component 
	 */
	public Component getSelectedComponent()
	{
		// get the selected Treeitem
		Treeitem selectedItem = getTree().getSelectedItem();
					
		if (selectedItem == null)
			return null;
		
		// get the corresponding Component from the canvas
		Component selectedComponent = getCorrespondingCanvasComponent(selectedItem);

		// return the component from the canvas
		return selectedComponent;		
	}
	
	/**
	 * Retrieves the selected Treeitem and 
	 * returns the corresponding Component
	 * from the Canvas.
	 * @return The corresponding canvas component 
	 */
	public Component getCorrespondingCanvasComponent(Treeitem item)
	{
		if (item == null)
			return null;
		
		// get the Canvas component's Id, by removig the prefix
		// 'id_' from the Treeitem Id
		String sComponentId = StringUtils.removeStart(item.getId(), "id_");

		// get the corresponding Component from the canvas
		DesignerCanvas canvas = getDesigner().getCanvas();
		Component selectedComponent = canvas.getCanvasComponent(sComponentId);

		// return the component from the canvas
		return selectedComponent;		
	}
	
	/**
	 * Clears the designer Tree from  
	 */
	public void clearTree()
	{
		if (_tree == null)
			return;
		
		// clear all the Treeitems
		_tree.clear();
		
		Treechildren children = _tree.getTreechildren();
		
		if (children == null)
		{
			children = new Treechildren();
			_tree.appendChild(children);
		}
		
		// create a new Window as the first Treeitem
		// to start-off the design
		Window wndDefault = new Window();
		
		// first add the Window to the canvas
		getDesigner().getCanvas().appendChild(wndDefault);
		
		// then add a Treeitem to the designer Tree
		Treeitem item = getDesigner().getSynchronizer().createTreeitem(wndDefault, _tree);
		children.appendChild(item);
	}
	
	
	/**
	 * Class that implements a dialog
	 * displayed when the user changes
	 * the position of a canvas component
	 * within the Tree
	 * @author chris.spiliotopoulos
	 *
	 */
	public class MoveItemDialog extends Window
								implements EventListener
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= -3664909763479883786L;
	
		/*** Drag-and-drop Positions ***/
		
		/**
		 * Do nothing 
		 */
		public static final int DD_NONE		= 0;
		
		/**
		 * Append the component as a child to the target 
		 */
		public static final int DD_AS_CHILD		= 1;
				
		/**
		 * Append the target as a child to the component 
		 */
		public static final int DD_AS_PARENT 	= 2;
		
		/**
		 * Insert the component before the  
		 */
		public static final int DD_BEFORE	 	= 3;
		
		/**
		 * Insert the component after the target 
		 */
		public static final int DD_AFTER	 	= 4;
		
		/**
		 * The selected move  
		 */
		private int _nSelectedMove = MoveItemDialog.DD_NONE;
		
		/**
		 * The designer Tree 
		 */
		protected DesignerTree _tree = null;
		
		/**
		 * @param designer
		 */
		public MoveItemDialog(DesignerTree tree)
		{
			_tree = tree;
		}
		
		// Getters / Setters
		public int getMoveType() { return _nSelectedMove; }
		
		/**
		 * Create the dialog
		 */
		public void onCreate()
		{
			// create the window
			setId("wndMoveDialog");
			setTitle("Move Component");
			setWidth("350px");
			setBorder("normal");
			setClosable(true);
	
			Hbox hbox = new Hbox();
			appendChild(hbox);
			
			Button btnChild = new Button("Append as child");
			btnChild.setId("btnChild");
			btnChild.setWidth("100px");
			btnChild.addEventListener("onClick", this);
			this.appendChild(btnChild);
			
			Button btnParent = new Button("Set as parent");
			btnParent.setId("btnParent");
			btnParent.setWidth("100px");
			btnParent.addEventListener("onClick", this);
			this.appendChild(btnParent);
			
			Button btnBefore = new Button("Insert before");
			btnBefore.setId("btnBefore");
			btnBefore.setWidth("100px");
			btnBefore.addEventListener("onClick", this);
			this.appendChild(btnBefore);
			
			hbox.appendChild(btnChild);
			hbox.appendChild(btnBefore);
			hbox.appendChild(btnParent);
			
			_designer.appendChild(this);
		}

		/* (non-Javadoc)
		 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
		 */
		public void onEvent(Event evt)
		{
			/*** Event: Add as child ***/
			if (evt.getTarget().getId().equals("btnChild"))
				_nSelectedMove = MoveItemDialog.DD_AS_CHILD;
			
			/*** Event: Set as parent ***/
			if (evt.getTarget().getId().equals("btnParent"))
				_nSelectedMove = MoveItemDialog.DD_AS_PARENT;
			
			/*** Event: Insert before ***/
			if (evt.getTarget().getId().equals("btnBefore"))
				_nSelectedMove = MoveItemDialog.DD_BEFORE;
			
			/*** Event: Insert after ***/
			if (evt.getTarget().getId().equals("btnAfter"))
				_nSelectedMove = MoveItemDialog.DD_AFTER;
			
			// end the modal thread
			doEmbedded();
			
			// close the 'Move Component' window
			detach();
		}

		/* (non-Javadoc)
		 * @see com.potix.zk.ui.event.EventListener#isAsap()
		 */
		public boolean isAsap()
		{
			return true;
		}
	}
	
	/**
	 * Inner class that implements a new context
	 * popup menu for a specified Treeitem.
	 * @author chris.spiliotopoulos
	 *
	 */
	public class TreeitemContextMenu extends Menupopup
								     implements EventListener
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= -1906082804766707695L;

		/**
		 * Menu Action: COPY ELEMENT 
		 */
		public static final int ACTION_COPY		= 1;
		
		/**
		 * Menu Action: PASTE ELEMENT 
		 */
		public static final int ACTION_PASTE 	= 2;
		
		/**
		 * Menu Action: DELETE ELEMENT 
		 */
		public static final int ACTION_DELETE 	= 3;
		
		/**
		 * Menu Action: VIEW PROPERTIES 
		 */
		public static final int ACTION_VIEW_PROPERTIES 	= 4;
		
		/**
		 * Menu Action: VIEW EVENTS 
		 */
		public static final int ACTION_VIEW_EVENTS 	= 5;
		
		/**
		 * Creates the popup menu
		 */
		public void onCreate()
		{
			setId("treeitemPopup");
			
			// create the 'Copy Element' menu item
			Menuitem itemCopy = new Menuitem("Copy Element");
			Image img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/popup_menu/element_copy.png");
			itemCopy.setImageContent(img.getContent());
			itemCopy.setId("mnuCopyElement");
			itemCopy.addEventListener("onClick", this);
			appendChild(itemCopy);
			
			// create the 'Paste Element' menu item
			Menuitem itemPaste = new Menuitem("Paste Element");
			img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/popup_menu/element_paste.png");
			itemPaste.setImageContent(img.getContent());
			itemPaste.setId("mnuPasteElement");
			itemPaste.addEventListener("onClick", this);
			itemPaste.setVisible(false);					// initialy hidden
			appendChild(itemPaste);	
			
			// create the 'Delete Element' menu item
			Menuitem itemDelete = new Menuitem("Delete Element");
			img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/popup_menu/element_delete.png");
			itemDelete.setImageContent(img.getContent());
			itemDelete.setId("mnuDeleteElement");
			itemDelete.addEventListener("onClick", this);
			appendChild(itemDelete);
		
			// create the 'View Properties' menu item
			Menuitem itemProperties = new Menuitem("View Properties");
			img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/popup_menu/view_properties.png");
			itemProperties.setImageContent(img.getContent());
			itemProperties.setId("mnuViewProperties");
			itemProperties.addEventListener("onClick", this);
			appendChild(itemProperties);	
			
			// create the 'View Events' menu item
			Menuitem itemEvents = new Menuitem("View Events");
			img = ComponentFactory.createImage(DesignerTree.class.getName(), "images/designer/tree/popup_menu/view_events.png");
			itemEvents.setImageContent(img.getContent());
			itemEvents.setId("mnuViewEvents");
			itemEvents.addEventListener("onClick", this);
			appendChild(itemEvents);
		}
		
		/**
		 * Returns the Menuitem that represents
		 * the given action
		 * @param nAction One of the following values:
		 * <li><b>TreeitemContextMenu.ACTION_COPY</b>
		 * <li><b>TreeitemContextMenu.ACTION_PASTE</b>
		 * @return
		 */
		public Menuitem getMenuItem(int nAction)
		{
			Menuitem item = null;
			
			// return the required menu item
			// based on the specified ACTION
			switch (nAction)
			{
				case TreeitemContextMenu.ACTION_COPY:
					item = (Menuitem) getFellow("mnuCopyElement");
					break;
				
				case TreeitemContextMenu.ACTION_PASTE:
					item = (Menuitem) getFellow("mnuPasteElement");
					break;	
					
				case TreeitemContextMenu.ACTION_DELETE:
					item = (Menuitem) getFellow("mnuDeleteElement");
					break;	
					
				case TreeitemContextMenu.ACTION_VIEW_PROPERTIES:
					item = (Menuitem) getFellow("mnuViewProperties");
					break;	
				
				case TreeitemContextMenu.ACTION_VIEW_EVENTS:
					item = (Menuitem) getFellow("mnuViewEvents");
					break;	
			}
		
			return item;
		}
		
		/* (non-Javadoc)
		 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
		 */
		public void onEvent(Event evt)
		{
			try
			{
				if (_tree == null)
					return;

				// get the currently selected Treeitem
				Treeitem itemSelected = _tree.getSelectedItem();
				
				if (itemSelected == null)
					return;
				
				/*** Copy Treeitem ***/
				if (evt.getTarget().getId().equals("mnuCopyElement"))
				{
					// copy the selected Treeitem
					copyTreeitem(itemSelected.getId());
				}
				
				/*** Paste Treeitem ***/
				if (evt.getTarget().getId().equals("mnuPasteElement"))
				{
					// paste the copied Treeitem to the
					// selected target Treeitem
					pasteTreeitem(itemSelected.getId());
				}
				
				/*** Delete Treeitem ***/
				if (evt.getTarget().getId().equals("mnuDeleteElement"))
				{
					// paste the copied Treeitem to the
					// selected target Treeitem
					deleteTreeitem(itemSelected.getId());
				}
				
				/*** View Properties ***/
				if (evt.getTarget().getId().equals("mnuViewProperties"))
				{
					// display the properties of the selected component
					displayComponentProperties(itemSelected.getId(), PropertiesWindow.VIEW_PROPERTIES);
				}
				
				/*** View Events ***/
				if (evt.getTarget().getId().equals("mnuViewEvents"))
				{
					// display the events of the selected component
					displayComponentProperties(itemSelected.getId(), PropertiesWindow.VIEW_EVENTS);
				}
			}
			catch (Exception e)
			{
				
			}
		}

		/* (non-Javadoc)
		 * @see com.potix.zk.ui.event.EventListener#isAsap()
		 */
		public boolean isAsap() { return true; }
		
		/**
		 * Keeps in memory the Id of the selected
		 * Treeitem to be copied.
		 * @param sId The selected Treeitem's Id
		 * [source]
		 */
		private void copyTreeitem(String sSourceId)
		{
			if (StringUtils.isEmpty(sSourceId))
			{
				_sElementToPasteId = "";
				return;
			}
		
			// get the canvas instance
			DesignerCanvas canvas = getDesigner().getCanvas();
			
			// get the copied component [source]
			Component sourceComponent = canvas.getCanvasComponent(getCanvasComponentId(sSourceId));
			
			try
			{
				// run any Copy rules
				RulesEngine.applyRules(sourceComponent, RulesEngine.COPY_RULES);
			}
			catch (RulesException e)
			{
				try
				{
					if (e instanceof DisplayableRulesException)
						Messagebox.show(e.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);	
				}
				catch (Exception e1)
				{
				}
				
				return;
			}
			
			// keep the selected element's Id on the clipboard
			// and enable the 'Paste' action on the context menu
			_sElementToPasteId = sSourceId;
			_menu.getMenuItem(TreeitemContextMenu.ACTION_PASTE).setVisible(true);
		}
	
		/**
		 * Creates a clone of the canvas element
		 * that was recently copied and pastes it
		 * onto the currently selected element.
		 * @param sId The selected Treeitem's Id
		 * [target]
		 */
		private void pasteTreeitem(String sTargetId)
		{
			if ((StringUtils.isEmpty(_sElementToPasteId)) || (StringUtils.isEmpty(sTargetId)))
				return;
		
			try
			{
				// get the canvas instance
				DesignerCanvas canvas = getDesigner().getCanvas();
				
				// get the copied component [source]
				Component sourceComponent = canvas.getCanvasComponent(getCanvasComponentId(_sElementToPasteId));
				
				// get the canvas component that corresponds to the 
				// currently selected Treeitem [target]
				Component targetComponent = canvas.getCanvasComponent(getCanvasComponentId(sTargetId));
				
				if ((sourceComponent == null) || (targetComponent == null))
					return;

				// clone the source component
				Component cloneComponent = (Component) sourceComponent.clone();
				
				if (cloneComponent == null)
					return;

				// assign new Ids to all the elements contained 
				// within the cloned component
				cloneComponent = ComponentFactory.assignNewIds(cloneComponent);
				
				try
				{
					// run any Pre-Paste rules
					RulesEngine.applyRules(cloneComponent, targetComponent, RulesEngine.PRE_PASTE_RULES);
				}
				catch (RulesException e)
				{
					try
					{
						if (e instanceof DisplayableRulesException)
							Messagebox.show(e.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);	
					}
					catch (Exception e1)
					{
					}
					
					return;
				}
				
				// append the clone as a child to the target component
				targetComponent.appendChild(cloneComponent);
				
				// synchronize the tree with the canvas
				getDesigner().getSynchronizer().synchronizeTreeWithCanvas(canvas);
				
				// disable the 'Paste' action from the popup menu 
				// and clear the clipboard
				_menu.getMenuItem(TreeitemContextMenu.ACTION_PASTE).setVisible(false);
				_sElementToPasteId = "";
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Keeps in memory the Id of the selected
	 * Treeitem to be copied.
	 * @param sId The selected Treeitem's Id
	 * [source]
	 */
	private void deleteTreeitem(String sSelectedItemId)
	{
		try
		{
			// get the Treeitem that was selected by the user
			Treeitem selectedItem = _tree.getSelectedItem();
			Treeitem root = (Treeitem) _tree.getItems().toArray()[0];
			
			if (selectedItem.getUuid().equals(root.getUuid()))
			{
				// display an error message and exit
				Messagebox.show("Root component cannot be deleted", "Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}

			// get the canvas
			DesignerCanvas canvas = getDesigner().getCanvas();
			
			// get the corresponding canvas component
			Component selectedComponent = canvas.getCanvasComponent(getCanvasComponentId(sSelectedItemId));
			
			// remove the component from the canvas
			if (selectedComponent != null)
			{
				// dispose the component's resources first
				ComponentFactory.disposeComponent(selectedComponent);
				
				Components.removeAllChildren(selectedComponent);
				selectedComponent.detach();
			}
			
			// remove the item from the tree
			_tree.removeChild(selectedItem);
			selectedItem.detach();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays the properties of the selected Tree component
	 * on a modal window
	 * @param selectedComponent
	 */
	protected void displayComponentProperties(String sId, 
											  int nViewMode)
	{
		try
		{
			// get the canvas
			DesignerCanvas canvas = getDesigner().getCanvas();
			
			// get the canvas component that corresponds to the 
			// currently selected Treeitem [target]
			Component selectedComponent = canvas.getCanvasComponent(getCanvasComponentId(sId));
			
			if (selectedComponent == null)
				return;
			
			// create a new Properties window and display it as modal
			PropertiesWindow wndProps = new PropertiesWindow(getDesigner(), 
					                                         (AbstractComponent) selectedComponent, 
					                                         nViewMode);			
			
			// create the window and append it to the designer
			wndProps.onCreate();
			getDesigner().appendChild(wndProps);
			
			// display it as popup
			wndProps.doModal();
			
			// detach the window
			wndProps.detach();
			wndProps = null;
			
			// invalidate the component
			selectedComponent.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the equivalent canvas component Id
	 * from the specified Treeitem Id that represents
	 * this component on the model tree view.
	 * @param sTreeitemId The Treeitem Id
	 * @return The corresponding canvas Id for the specified
	 * item
	 */
	private String getCanvasComponentId(String sTreeitemId)
	{
		if (StringUtils.isEmpty(sTreeitemId))
			return "";
		
		// strip the Treeitem's Id from the 'id_' prefix
		// in order to the the corresponding canvas Id
		sTreeitemId = StringUtils.removeStart(sTreeitemId, "id_");
		
		// return the corresponding canvas component Id
		return sTreeitemId;
	}
}