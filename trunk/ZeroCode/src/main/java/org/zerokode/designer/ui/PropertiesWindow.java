package org.zerokode.designer.ui;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.IDisposable;
import org.zerokode.designer.model.ComponentFactory;
import org.zerokode.designer.model.ZUMLModel;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.metainfo.ZScript;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;


/**
 * Implements a modal window that displays
 * a selected designer Component's properties
 * and allows the user to modify them
 * @author chris.spiliotopoulos
 *
 */
public class PropertiesWindow extends DesignerWindow
								implements EventListener, 
									       IDisposable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1746981195735430037L;

	/**
	 * Properties view mode 
	 */
	public static final int VIEW_PROPERTIES = 1;
	
	/**
	 * Events view mode 
	 */
	public static final int VIEW_EVENTS 	= 2;
	
	/**
	 * The active view mode 
	 */
	private int _nViewMode = VIEW_PROPERTIES;
	
	/**
	 * The selected Component to be investigated 
	 */
	protected AbstractComponent _component = null;
	
	/**
	 * The properties grid
	 */
	protected Grid _gridProps = null;
	
	/**
	 * The events grid
	 */
	protected Grid _gridEvents = null;
	
	/**
	 * A map that contains all the Properties values 
	 */
	protected HashMap _mapProps = null;
	
	/**
	 * A map that contains all the Event handlers 
	 */
	protected HashMap _mapEvents = null;
	
	/**
	 * Default constructor
	 * @param designer
	 */
	protected PropertiesWindow(Designer designer)
	{
		super(designer);
	}

	/**
	 * 2-argument Constructor
	 * @param designer The designer object
	 * @param selectedComponent The selected component 
	 * whose properties will be investigated
	 */
	public PropertiesWindow(Designer designer, 
							AbstractComponent selectedComponent, 
							int nViewMode)
	{
		// call daddy
		super(designer);
		
		_component = (AbstractComponent) selectedComponent;
		_nViewMode = nViewMode; 
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.ui.DesignerWindow#onCreate()
	 */
	public void onCreate()
	{
		// create the Properties window
		setId("wndProperties");
		setTitle("Component Properties");
		setWidth("400px");
		setBorder("normal");
		setClosable(true);

		if (_nViewMode == VIEW_PROPERTIES)
		{
			// display the component properties on a grid
			displayComponentProperties();
		}
		else if (_nViewMode == VIEW_EVENTS)
		{
			// display the component events on a grid
			displayComponentEvents();
		}
	}


	/** 
	 * Display the component properties 
	 * on a grid.
	 */
	protected void displayComponentProperties()
	{
		if (_component == null)
			return;
		
		// set the window's title to display the Component's class
		this.setTitle(ComponentFactory.getSimpleClassName(_component) + " Properties");
		
		// get the propery descriptors of the Component class
		PropertyDescriptor[] arrDescriptors = ComponentFactory.getComponentProperties(_component);	
		
		if (ArrayUtils.isEmpty(arrDescriptors))
			return;
	
		// create the grid and attach it to the 
		// Properties tab panel
		_gridProps = new Grid();
		_gridProps.setHeight("200px");
		_gridProps.setId("gridProperties");
		appendChild(_gridProps);
		
		// create the columns
		Columns cols = new Columns();
		Column colName = new Column();
		Column colValue = new Column();
		colName.setWidth("80px");
		colName.setLabel("Property Name");
		colValue.setLabel("Value");
		_gridProps.appendChild(cols);
		cols.appendChild(colName);
		cols.appendChild(colValue);
		
		// create the Rows object
		Rows rows = new Rows();
		_gridProps.appendChild(rows);
		
		// create a new map to hold all the property values
		if (_mapProps == null)
			_mapProps = new HashMap();
		
		// loop through the component property descriptors
		for (int i = 0; i < arrDescriptors.length; i++)
		{
			try
			{
				// get the next property descriptor
				PropertyDescriptor descriptor = arrDescriptors[i];
				
				if (descriptor == null)
					continue;
				
				// get the name of the object property
				String sName = descriptor.getName();
				
				Object value = null;
				
				try
				{
					// get the Property's value
					value = BeanUtils.getProperty(_component, descriptor.getName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// create a new Grid row
				Row row = new Row();
				rows.appendChild(row);
				
				// add the Property name at the 1st column
				Label lblName = new Label();
				lblName.setValue(sName);
				row.appendChild(lblName);
				
				// add the Property value at the 2nd column
				
				// generic Component object
				Component cmpValue = null;
				
				/*** Boolean Values  ***/
				if (descriptor.getPropertyType() == boolean.class)
				{
					// for boolean properties, display
					// a list box with the 'True' or 'False' 
					// list items
					Listbox list = new Listbox();
					list.setMold("select");
					list.setWidth("90%");
					row.appendChild(list);
					
					Listitem itemTrue = new Listitem("true");
					list.appendChild(itemTrue);
					
					Listitem itemFalse = new Listitem("false");
					list.appendChild(itemFalse);
					
					// display the current property value
					// on the combobox
					boolean bValue = true;
					
					if (value != null)
						bValue = Boolean.valueOf((String) value).booleanValue();
					
					if (bValue)
						list.setSelectedItem(itemTrue);
					else
						list.setSelectedItem(itemFalse);
					
					cmpValue = list;
				}
				else 
				{
					/*** Rest of data types ***/
					
					// convert the value Object into the correct
					// data type for display
					String sValue = "";
					
					if (value == null)
						value = "";
					
					if (value instanceof String)
						sValue = (String) value;
					else 
						sValue = String.valueOf(value);
					
					// for any other property type, display a 
					// textbox component 
					Textbox textbox = new Textbox();
					textbox.setWidth("95%");
					textbox.setMaxlength(1000);
					
					// if the size of the value string is > 50 chars, 
					// set the Textbox to multiline, so that the value
					// is visible
					if (sValue.length() > 50)
						textbox.setMultiline(true);
					
					// display the value on the Textbox
					textbox.setValue(sValue);
					row.appendChild(textbox);
				
					cmpValue = textbox;
				}
				
				// add the Textbox objects to the Hashmap, 
				// using their Ids as the key
				_mapProps.put(sName, cmpValue);

			}
			catch (Exception e)
			{
				continue;
			}
		}
		
		// append the 'Update' button
		Button btnUpdate = new Button("Update");
		btnUpdate.setId("btnUpdate");
		btnUpdate.addEventListener("onClick", this);
		appendChild(btnUpdate);
	}

	/** 
	 * Display the component events 
	 * on a grid.
	 */
	protected void displayComponentEvents()
	{
		if (_component == null)
			return;
		
		// get all the events that apply to this component
		String[] arrEvents = ComponentFactory.getComponentEvents(_component.getClass());
		
		if (ArrayUtils.isEmpty(arrEvents))
		{
			Label lbl = new Label();
			lbl.setValue("The selected element [" + _component.getClass().getSimpleName() + 
					     "] has no events...");
			appendChild(lbl);
			
			// append the 'Ok' button. No update will take place
			Button btnUpdate = new Button("Ok");
			btnUpdate.setId("btnUpdate");
			btnUpdate.addEventListener("onClick", this);
			appendChild(btnUpdate);

			return;
		}
		
		// create the grid and attach it to the 
		// Properties tab panel
		_gridEvents = new Grid();
		_gridEvents.setHeight("200px");
		_gridEvents.setId("gridEvents");
		appendChild(_gridEvents);
		
		// create the columns
		Columns cols = new Columns();
		Column colName = new Column();
		Column colValue = new Column();
		colName.setWidth("75px");
		colName.setLabel("Event");
		colValue.setLabel("Handler");
		_gridEvents.appendChild(cols);
		cols.appendChild(colName);
		cols.appendChild(colValue);
		
		// create the Rows object
		Rows rows = new Rows();
		_gridEvents.appendChild(rows);
		
		// create a new map to hold all the property values
		if (_mapEvents == null)
			_mapEvents = new HashMap();

		// iterate through the events array
		for (int i = 0; i < arrEvents.length; i++)
		{
			try
			{
				// get the next event's name
				String sEventName = StringUtils.trim(arrEvents[i]);
				
				// check if this event has a defined event handler (script)
				// [we don't care about event listeners, as we are after ZUML
				//  elements only - listeners are code elements]
				ZScript zScript = ((ComponentCtrl)_component).getEventHandler(sEventName);
				String sScript = zScript != null ? zScript.getContent(null, null): "";
				
				// create a new Grid row
				Row row = new Row();
				rows.appendChild(row);
				
				// add the Event name at the 1st column
				Label lblName = new Label();
				lblName.setValue(sEventName);
				row.appendChild(lblName);
				
				// add the Handler value at the 2nd column
				
				// for any other property type, display a 
				// textbox component 
				Textbox textbox = new Textbox();
				textbox.setWidth("95%");
				textbox.setMaxlength(1000);
				
				// if the size of the value string is > 50 chars, 
				// set the Textbox to multiline, so that the value
				// is visible
				if (sScript.length() > 50)
					textbox.setMultiline(true);
				
				// display the value on the Textbox
				textbox.setValue(sScript);
				row.appendChild(textbox);
				
				// add the Textbox objects to the Hashmap, 
				// using their Ids as the key
				_mapEvents.put(sEventName, textbox);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		// clean up
		for (int i = 0; i < arrEvents.length; i++)
			arrEvents[i] = null;
		
		// append the 'Update' button
		Button btnUpdate = new Button("Update");
		btnUpdate.setId("btnUpdate");
		btnUpdate.addEventListener("onClick", this);
		appendChild(btnUpdate);
	}
	
	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#onEvent(com.potix.zk.ui.event.Event)
	 */
	public void onEvent(Event evt)
	{
		/*** Event: Update Property Values ***/
		if (evt.getTarget().getId().equals("btnUpdate"))
		{
			if (_nViewMode == VIEW_PROPERTIES)
			{
				// update the Properties
				updatePropertyValues();
			}
			else if (_nViewMode == VIEW_EVENTS)
			{
				// update the Event handlers
				updateEventHandlers();
			}
		}
		
		// close the window
		onClose();
	}

	/* (non-Javadoc)
	 * @see com.potix.zk.ui.event.EventListener#isAsap()
	 */
	public boolean isAsap()
	{
		return true;
	}
	
	/**
	 * Retrieves the latest property values
	 * of a component which are displayed on the 
	 * property grid and updates the actual object.
	 */
	protected void updatePropertyValues()
	{
		if ((_mapProps == null) || (_mapProps.isEmpty()))
			return;
	
		// generic Component object
		AbstractComponent cmpValue = null;
		
		// get the set of keys contained in the properties 
		// hashmap
		Set keys = _mapProps.keySet();

		// loop through the grid's property controls
		Iterator iterKeys = keys.iterator();
		while (iterKeys.hasNext())
		{
			try
			{
				// get the key [Component Id]
				String sKey = (String) iterKeys.next();
				
				// get the property component
				cmpValue = (AbstractComponent) _mapProps.get(sKey);

				// get the descriptor for this property
				PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(_component, sKey);
				
				// set the new Property values, depending on
				// the property type
				if (cmpValue.getClass().getSimpleName().equals("Textbox"))
				{
					/*** Textbox property control ***/
					
					// get the property value from the textbox
					String sValue = ((Textbox) cmpValue).getValue();
					
					// get the property's argument class
					Class clazzProp = descr.getPropertyType();

					// cast the component to its runtime class first
					// and then set its value
					if ((clazzProp == String.class))
						PropertyUtils.setProperty(_component, sKey, sValue);
					else if (clazzProp == int.class)
						PropertyUtils.setProperty(_component, sKey, Integer.valueOf(sValue));
					else if (clazzProp == long.class)
						PropertyUtils.setProperty(_component, sKey, Long.valueOf(sValue));
				}
				else if (cmpValue.getClass().getSimpleName().equals("Listbox"))
				{
					/*** Combobox property control ***/

					// get the Listbox
					Listbox list = (Listbox) cmpValue;
					
					// get the selected item from the Listbox
					Listitem item = list.getSelectedItem();
					
					Boolean bValue = new Boolean((String) item.getLabel());
					
					// set the new value
					PropertyUtils.setProperty(_component, sKey, bValue);
				}
			}
			catch (Exception e)
			{
				continue;
			}
		}
	}

	/**
	 * Retrieves the latest property values
	 * of a component which are displayed on the 
	 * property grid and updates the actual object.
	 */
	protected void updateEventHandlers()
	{
		if ((_mapEvents == null) || (_mapEvents.isEmpty()))
			return;

		// generic Component object
		Component cmpValue = null;
		
		// get the set of keys contained in the properties 
		// hashmap
		Set keys = _mapEvents.keySet();
	
		try
		{	
			// get the ZUML representation of the current model
			ZUMLModel model = getDesigner().getCanvas().getZUMLRepresentation();
			
			if (model == null)
				return;
			
			// loop through the grid's property controls
			Iterator iterKeys = keys.iterator();
			while (iterKeys.hasNext())
			{
				try
				{
					// get the key [Event name]
					String sEventName = (String) iterKeys.next();
					
					// get the event handler textbox
					cmpValue = (Component) _mapEvents.get(sEventName);
					
					// get the event handling script (textbox value)
					String sScript = ((Textbox) cmpValue).getValue();
					
					if (sScript == null)
						sScript = "";
					
					// add the event handler directly to the iDOM element
					model.addEventHandler(_component, sEventName, sScript);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					
					continue;
				}
			}
			
			// reload the model without refreshing the tree, 
			// so that the updated event handlers will be activated
			getDesigner().getCanvas().loadModelFromDocument(model.getZUMLDocument(), false);
		
			// clean up
			model.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// clean up
		_mapEvents = null;
		_mapProps = null;
		
		if (_gridEvents != null)
		{
			Components.removeAllChildren(_gridEvents);
			_gridEvents = null;
		}
		
		if (_gridProps != null)
		{
			Components.removeAllChildren(_gridProps);
			_gridProps = null;
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

	/* (non-Javadoc)
	 * @see com.potix.zul.html.Window#onClose()
	 */
	public void onClose()
	{
		// close the window
		this.doEmbedded();
		
		// clean up
//		dispose();
	
		getDesigner().cleanUpMemory();
		
		super.onClose();
	}
}
