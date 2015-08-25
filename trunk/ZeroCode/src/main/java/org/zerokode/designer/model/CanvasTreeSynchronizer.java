package org.zerokode.designer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.zerokode.designer.IDisposable;
import org.zerokode.designer.config.Configurator;
import org.zerokode.designer.events.listeners.TreeEventListener;
import org.zerokode.designer.model.rules.engine.RulesEngine;
import org.zerokode.designer.ui.Designer;
import org.zerokode.designer.ui.DesignerCanvas;
import org.zerokode.designer.ui.DesignerToolkit;
import org.zerokode.designer.ui.DesignerTree;
import org.zkoss.idom.Element;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;


/**
 * Class that performs synchronization
 * between the model displayed on the 
 * designer Canvas and the designer Tree.
 * @author chris.spiliotopoulos
 *
 */
public class CanvasTreeSynchronizer implements IDisposable
{
	/**
	 * The designer window 
	 */
	protected Designer _wndDesigner = null;
	
	/**
	 * The Tree level that is being processed  
	 */
	private int _nCurrentLevel = 0;
	
	/**
	 *  
	 */
	private HashMap _mapTreechildren = null;
	
	/**
	 * Drag-and-drop target Treeitem  
	 */
	private String _targetTreeitemId = "";
	
	/**
	 * Default constructor 
	 */
	public CanvasTreeSynchronizer(Designer wndDesigner)
	{
		_wndDesigner = wndDesigner;
	}
	
	// Getters / Setters
	public void setTargetTreeitem(String targetTreeitemId) { _targetTreeitemId = targetTreeitemId; }
	public String getTargetTreeitem() { return _targetTreeitemId; }
	
	/**
	 * Synchronizes the Tree with the component model 
	 * that is displayed on the canvas
	 * @param root The root component of the Canvas
	 */
	public void synchronizeTreeWithCanvas(DesignerCanvas wndCanvas)
	{
		if ((wndCanvas == null) || (_wndDesigner == null))
			return;
	
		// get the Tree window
		DesignerTree wndTree = (DesignerTree) _wndDesigner.getFellow("designerTree");
		
		if (wndTree == null)
			return;
		
		// get the Tree component
		Tree tree = wndTree.getTree();
		
		// remove all the Tree items
		tree.clear();
		
		// reset the indices
		_nCurrentLevel 	= 0;
		
		if (_mapTreechildren != null)
		{
			_mapTreechildren.clear();
			_mapTreechildren = null;
		}
		
		// set the canvas window height to 100%
		// in order to accomodate smoothly all the
		// components
		wndCanvas.setHeight("100%");
		
		// parse the Component model displayed on the canvas
		// and display it on the Tree
		parseComponentModel(wndCanvas, tree);
	
	}
	
	
	/**
	 * Parses the input Component model that is
	 * currently displayed on the designer canvas
	 * and displays each discreet UI component as 
	 * a Tree item on the designer Tree.
	 * @param wndCanvas The designer Canvas
	 * @param tree The designer Tree
	 */
	protected void parseComponentModel(Component canvasComponent, 
									   Tree tree)
	{
		if ((canvasComponent == null) || (tree == null))
			return;
		
		if (_mapTreechildren == null)
			_mapTreechildren = new HashMap();
		
		// if the component still has an auto-Id assigned fix it
		canvasComponent.setId(ComponentFactory.fixAutoId(canvasComponent.getId()));

		// check if the component's children should be displayed
		// onto the model treeview. If not, exit now
		if (! RulesEngine.getComponentFlag(canvasComponent, RulesEngine.FLAG_SHOW_CHILDREN))
		{
			// decrease the current Tree level
			if (_nCurrentLevel > 0)
				_nCurrentLevel--;
			
			return;
		}
		
		// get the component's children
		List listChildren = canvasComponent.getChildren();

		if ((listChildren == null) || (listChildren.size() == 0))
		{
			// decrease the current Tree level
			if (_nCurrentLevel > 0)
				_nCurrentLevel--;
			
			return;
		}
		
		// loop through all the component's children
		Iterator iter = listChildren.iterator();
		while (iter.hasNext())
		{
			// get the next component in the list
			Component child = (Component) iter.next();
			
			if (child == null)
				continue;

			// if the component still has an auto-Id assigned fix it
			child.setId(ComponentFactory.fixAutoId(child.getUuid()));
			
			// add the Component to the designer Tree
			addComponentToTree(tree, child);
			
			// increase the current Tree level
			_nCurrentLevel++;
			
			/*** RECURSION ***/
			parseComponentModel(child, tree);
		}
		
		// decrease the current Tree level
		if (_nCurrentLevel > 0)
			_nCurrentLevel--;
	}
	
	/**
	 * Adds a canvas Component description on the
	 * designer Tree
	 * @param tree 
	 * @param canvasComponent
	 * @param parent
	 */
	public void addComponentToTree(Tree tree,
			   			           Component canvasComponent)
	{
		if ((tree == null) || (canvasComponent == null))
			return;

		try
		{
			// create a new Treeitem based on the 
			// specified canvas Component's properties
			Treeitem item = createTreeitem(canvasComponent, tree);
			
			if (item == null)
				return;
			
			// get the parent Treeitems collection from the Tree
			Collection clItems = tree.getItems();
			
			// get the parent Treeitem from the Treeitem collection
			Treechildren treeChildren = null;
		
			// format the key for the map
			String sKey = canvasComponent.getUuid() + "_" + String.valueOf(_nCurrentLevel);
			
			// if the component has any children, 
			// create and attach to it a Treechildren 
			// object and keep a reference to it

		    // add the Treechildren object to the Hashmap
			// and use as the key the current Tree level
			// (but only if it doesn't exist in the map)
			if (_mapTreechildren.get(sKey) == null)
			{
				if (canvasComponent.getChildren().size() > 0)
				{
					treeChildren = new Treechildren();
					item.appendChild(treeChildren);
				
					_mapTreechildren.put(sKey, treeChildren);
				}
			}
			
			// if the Tree size is 0, then...
			if (clItems.size() == 0)
			{
				// if the size is 0, we have to add the item to 
				// the ROOT Treechildren collection
				treeChildren = (Treechildren) tree.getTreechildren();
				
				if (treeChildren != null)
					treeChildren.appendChild(item);
				
				// add the Treechildren object to the map
				if (_mapTreechildren.get(sKey) == null)
					_mapTreechildren.put(sKey, treeChildren);
			}
			else
			{
				// get the Treechildren object where this item should
				// be appended to
				
				// get the Treechildren object from the map that
				// is attached to the component's parent
				sKey = canvasComponent.getParent().getUuid() + "_" + String.valueOf(_nCurrentLevel - 1);
				treeChildren = (Treechildren) _mapTreechildren.get(sKey);
				
				// append the child Treeitem to its parent
				if (treeChildren != null)
					treeChildren.appendChild(item);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new Treeitem based on the properties
	 * of the given canvas Component.
	 * @param canvasComponent The specified Canvas Component
	 * @return The new Treeitem
	 */
	public Treeitem createTreeitem(Component canvasComponent, 
								   Tree tree)
	{
		try
		{
			// create a new Treeitem to be appended to 
			// the designer Tree
			Treeitem item = new Treeitem();
			
			// set the item's Id, by using the Component's
			// Id plus the prefix 'id_'
			item.setId("id_" + canvasComponent.getUuid());
			
			// create a new Treerow that will contain 
			// the Component's Id with a small-scale image
			// of the component's type
			Treerow row = new Treerow();
			item.appendChild(row);
			
			// create the Id cell 
			Treecell cell = new Treecell();
			row.appendChild(cell);
			Label lbl = new Label(canvasComponent.getUuid() + " [" + ComponentFactory.getSimpleClassName(canvasComponent) + "]");
			cell.appendChild(lbl);
			row.setDraggable("treeItem");
			row.setDroppable("treeItem, toolkitComponent");
			row.addEventListener("onDrop", new TreeEventListener());
			
			// create the image cell
			Treecell cell2 = new Treecell();
			
			/*** Try to load the 16x16 image from the XML configuration file ***/
			/***                                                             ***/ 
			
			Image img16x16 = null;
			
			// get the active configurator instance
			Configurator config = DesignerToolkit.getComponentsConfigurator();
			
			if (config != null)
			{
				// get the iDOM description of the component, using the class
				// name as the filter
				Element domComponent = config.getElement("class", canvasComponent.getClass().getName(), null);
				
				if (domComponent != null)
				{
					// retrieve the 16x16 image URL
					Element domImage16 = config.getElement("image16", (Element) domComponent.getParent());
				
					// create the image from the specified URL source
					if (domImage16 != null)
						img16x16 = ComponentFactory.createImage(canvasComponent.getClass().getName(),
								                                domImage16.getText());
				}
			}

			if (img16x16 == null)
			{
				// if the image couldn't be created, assign the
				// component the 'Unknown' image
				img16x16 = ComponentFactory.createImage(getClass().getName(), 
												        "images/designer/components/unknown16.png");				
			}
			
			if (img16x16 != null)
				cell2.appendChild(img16x16);
			
			/***                                                             ***/
			/*** Try to load the 16x16 image from the XML configuration file ***/
			
			row.appendChild(cell2);

			// return the new Treeitem
			return item;
		}
		catch (Exception e)
		{
			
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// clean up
		_wndDesigner = null;
		_mapTreechildren = null;
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
