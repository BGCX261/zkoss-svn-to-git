package org.zerokode.designer.ui;

import org.apache.commons.lang.ArrayUtils;
import org.zerokode.designer.config.Configurator;
import org.zerokode.designer.model.ComponentFactory;
import org.zkoss.idom.Element;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Image;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;



/**
 * Class that utilizes the Widget View of the
 * designer, where the user selects widgets
 * and places them on the canvas.
 * @author chris.spiliotopoulos
 *
 */
public class DesignerToolkit extends DesignerGroupbox
{	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2144390995947399856L;
	/**
	 * The Tabbox container 
	 */
	protected Tabbox _tabBox = null;
	
	/**
	 * The Configurator instance used for loading
	 * configuration preferences 
	 */
	private static Configurator _config = null; 
	
	/**
	 * The total number of component groups 
	 * defined in the configuration file 'toolkit.xml' 
	 */
	private int _nGroupNum = 0;
	
	/**
	 * @param designer
	 */
	DesignerToolkit(Designer designer)
	{
		super(designer);
	}

	/**
	 * Returns the current Configurator object that holds 
	 * components information defined in the 'toolkit.xml' 
	 * configuration file
	 * @return The active components' Configurator object
	 */
	public static Configurator getComponentsConfigurator() { return _config; }
	
	/* (non-Javadoc)
	 * @see com.zk.designer.ui.DesignerWindow#onCreate()
	 */
	public void onCreate()
	{
		// create the Toolkit
		setId("designerToolkit");
		setWidth("400px");
		setMold("3d");
		setTooltiptext("Drag-and-drop widgets onto the model treeview in order to be added to the canvas.");

		// create the Caption
		Caption caption = new Caption();
		caption.setLabel("Component Toolkit");
		appendChild(caption);

		try
		{
			// load the 'toolkit.xml' configuration file
			_config = new Configurator(getDesigner().getWebApp().getRealPath("/config/toolkit/toolkit.xml"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// create the Tabs Accordion container
		createTabs();	
	}

	/**
	 * Creates the Accordion style Tabs container 
	 */
	protected void createTabs()
	{
		if (_config == null)
			return;
		
		try
		{
			// create the main Tabbox container
			_tabBox = new Tabbox();
			_tabBox.setWidth("100%");
			
			// add the Tabbox to the groupbox
			appendChild(_tabBox);
			
			// get the defined tab groups
			Element[] arrGroups = _config.getElements("group", null);
			
			if (ArrayUtils.isEmpty(arrGroups))
				return;
			
			// get the total number of component groups
			_nGroupNum = arrGroups.length;
			
			// create the tab groups
			createComponentGroups(arrGroups);
		}
		catch (Exception e)
		{
			// if the configuration file could not be loaded, exit
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Creates the individual component groups (tabs)
	 */
	protected void createComponentGroups(Element[] arrGroups)
	{
		if (ArrayUtils.isEmpty(arrGroups))
			return;
		
		// create a Tabs object for the group
		Tabs tabs = new Tabs();
		_tabBox.appendChild(tabs);
		
		// create a Tabpanels collection 
		Tabpanels panels = new Tabpanels();
		panels.setWidth("90%");
		_tabBox.appendChild(panels);
		
		// create the Tabs to accomodate the tab groups
		for (int i = 0; i < arrGroups.length; i++)
		{
			// get the next 'group' iDOM Element
			Element group = arrGroups[i];

			// if there are more than 1 component groups, then...
			if (_nGroupNum > 1)
			{
				// create the tab that displays the group name
				Tab tabGroup = new Tab(group.getAttribute("name"));
				tabs.appendChild(tabGroup);
				
				// create a panel for the group tab
				Tabpanel panelGroup = new Tabpanel();
				panels.appendChild(panelGroup);
					
				// create the component tabs within the group
				createComponentTabbox(group, panelGroup);
			}
			else
			{
				// create the component tabs within the group
				createComponentTabbox(group, null);
			}
		}
	}
	
	/**
	 * Creates a components tabbox within the 
	 * group for the specified element. 
	 * @param group
	 * @param panel
	 */
	protected void createComponentTabbox(Element domGroup, 
									     Tabpanel tabpanel)
	{
		if ((_config == null) || (domGroup == null))
			return;
		
		// get the tabs defined for this component
		// group, directly from the iDOM document
		Element[] arrTabs = _config.getElements("tab", domGroup);
		
		if (ArrayUtils.isEmpty(arrTabs))
			return;
		
		Tabbox box 			= null;
		Tabs tabs 			= null;
		Tabpanels panels 	= null;
		
		// if there are more than 1 component groups, then...
		if (_nGroupNum > 1)
		{
			// create an individual Tabbox to hold
			// the component tabs defined for this group
			box = new Tabbox();
			tabpanel.appendChild(box);
			
			// create the Tabs
			tabs = new Tabs();
			box.appendChild(tabs);
			
			// create the panels
			panels = new Tabpanels();
			box.appendChild(panels);
		}
		
		// create the Tabs to accomodate the tab groups
		for (int i = 0; i < arrTabs.length; i++)
		{
			// get the next 'group' iDOM Element
			Element elementTab = arrTabs[i];

			// create the tab that displays the group name
			Tab tab = new Tab(elementTab.getAttribute("name"));
			
			// if there are more than 1 component groups, then...
			if (_nGroupNum > 1)
				tabs.appendChild(tab);
			else
				_tabBox.getTabs().appendChild(tab);
			
			// create a panel for the group tab
			Tabpanel panel = new Tabpanel();
			
			// if there are more than 1 component groups, then...
			if (_nGroupNum > 1)
				panels.appendChild(panel);
			else
				_tabBox.getTabpanels().appendChild(panel);
			
			// add the predefined visual components to the tab
			addComponentsToTab(elementTab, panel);
		}
		
		// if there are more than one groups defined, 
		// then create an 'accordion' tabbox
		if (_nGroupNum > 1)
		{
			_tabBox.setMold("accordion");
		

		}
	}
	
	/**
	 * Retrieves the predefined visual components
	 * declared within the iDOM and attaches them 
	 * onto the specified tab panel 
	 * @param elementTab The iDOM description of the
	 * tab element
	 * @param tabpanel The panel where the components 
	 * will be attached to
	 */
	private void addComponentsToTab(Element domTab, 
								    Tabpanel tabpanel)
	{
		if ((_config == null) || (domTab == null) || (tabpanel == null))
			return;
	
		// get the components defined for this tab, 
		// directly from the iDOM document
		Element[] arrComponents = _config.getElements("component", domTab);
		
		if (ArrayUtils.isEmpty(arrComponents))
			return;
		
		// create the Tabs to accomodate the tab groups
		for (int i = 0; i < arrComponents.length; i++)
		{
			try
			{
				// get the next 'group' iDOM Element
				Element domComponent = arrComponents[i];
	
				// get the <class>, <image> and <tooltip> values
				// directly from the iDOM document
				Element domClass = _config.getElement("class", domComponent);
				Element domImage = _config.getElement("image32", domComponent);
				Element domTooltip = _config.getElement("tooltip", domComponent);
				
				if (domClass == null)
					continue;
				
				// create the component's image and attach it onto
				// the panel, according to its iDOM description
				addComponentImage(tabpanel, 
								  domClass.getText(), 
							      domImage.getText(), 
							      domTooltip.getText());
			}
			catch (Exception e)
			{
				// if something is missing, just move on
				continue;
			}
		}
	}
	
	/**
	 * Adds a component's image to the toolkit at
	 * the selected tab panel.
	 * @param panel
	 * @param sId
	 * @param sImgSrc
	 * @param sTooltip
	 */
	protected void addComponentImage(Tabpanel panel,
									 String sComponentClass,
									 String sImageSrc, 
									 String sTooltip)
	{
		try
		{
			// create and add the component's specified image
			Image img = ComponentFactory.createImage(sComponentClass, sImageSrc);
			
			if (img == null)
			{
				// if the image couldn't be created, assign the
				// component the 'Unknown' image
				img = ComponentFactory.createImage(getClass().getName(), 
												   "images/designer/components/unknown32.png");				
			}
			
			if (img == null)
				return;
			
			// set the component class as the image's Id
			img.setId(sComponentClass);
			
			// make it draggable
			img.setDraggable("toolkitComponent");
			
			// set the specified tooltip
			img.setTooltiptext(sTooltip);
			
			// and add it to the tab panel
			panel.appendChild(img);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}