package org.zerokode.designer.model;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.IDisposable;
import org.zerokode.designer.config.Configurator;
import org.zerokode.designer.model.rules.engine.RulesEngine;
import org.zerokode.designer.model.rules.engine.exceptions.DisplayableRulesException;
import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zerokode.designer.ui.Designer;
import org.zerokode.designer.ui.DesignerToolkit;
import org.zkoss.idom.Element;
import org.zkoss.image.AImage;
import org.zkoss.image.Image;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Strings;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.DesktopCtrl;


/**
 * Factory class for creating UI components
 * to be added onto the designer canvas.
 * @author chris.spiliotopoulos
 *
 */
public class ComponentFactory
{	
	/**
	 * Creates a new UI Component of the given class.
	 * @param cls The component's class
	 * @return The newly created component with default 
	 * properties
	 * @throws Exception 
	 */
	public static Component createComponent(String sComponentClass) throws Exception
	{
		if (StringUtils.isEmpty(sComponentClass))
			return null;
		
		// get an instance of the given component class
		Class componentClass = Class.forName(sComponentClass);

		// create a new instance of the specified component class
		Component newComponent = (Component) componentClass.newInstance();

		try
		{
			// check for any pre-creation rules
			RulesEngine.applyRules(newComponent, RulesEngine.PRE_CREATION_RULES);
		}
		catch (RulesException re)
		{
			// if a rules exception has been thrown, 
			// do not create the component on the canvas
			newComponent = null;	
			
			// if this is a displayable exception, 
			// display the message on a Messagebox
			// display an error message and exit
			if (re instanceof DisplayableRulesException)
			{
				Messagebox.show(re.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);
			}
			
			return null;
		}
		
		Method method = null;

		try
		{
			// check if the component implements the 'onCreate' method
			method = Classes.getAnyMethod(componentClass, "onCreate", null);

			// if yes, invoke it now to do custom initialization
			method.invoke(newComponent, (Object[])null);
		}
		catch (NoSuchMethodException e)
		{
		}
		
		// assign the component an Id
		newComponent.setId(fixAutoId(newComponent.getUuid()));
			
		// return the newly created component
		return newComponent;
	}
	
	/**
	 * Creates an Image component, using the given 
	 * application relative path
	 * @param sImgSrc The relative path to the image source
	 * @return The newly created Image
	 */
	public static URL getResourceUrl(String sComponentClass,
					  			     String sImgSrc)
	{
		if (StringUtils.isEmpty(sImgSrc))
			return null;
		
		URL url = null;
		
		if (! StringUtils.isEmpty(sComponentClass))
		{
			Class clazz = null;

			try
			{
				// get the component's runtime class
				clazz = Class.forName(sComponentClass);
				
				// try to get the URL of the image from a 
				// relative path to the class's physical location
				// (could be a web app path or a JAR file)
				url = clazz.getClassLoader().getResource(sImgSrc);
			}
			catch (ClassNotFoundException e)
			{
			}
		}
		
		if (url == null)
		{
			try
			{
				// try to get the image source from a path
				// relative to the web app path
				url = getResourceRelativePath(sImgSrc);
			}
			catch (Exception e)
			{
				return null;
			}
		}
		
		// return the URL of the specified resource
		return url;
	}
	
	
	/**
	 * Creates an Image component, using the given 
	 * application relative path
	 * @param sImgSrc The relative path to the image source
	 * @return The newly created Image
	 */
	public static org.zkoss.zul.Image createImage(String sComponentClass,
												  String sImgSrc)
	{
		// get the URL of the requested image resource
		URL url = getResourceUrl(sComponentClass, sImgSrc);
		
		// create the image from the URL
		return createImage(url);
	}

	/**
	 * Creates an Image component, using the given 
	 * application relative path
	 * @param sImgSrc The relative path to the image source
	 * @return The newly created Image
	 */
	public static org.zkoss.zul.Image createImage(URL url)
	{
		if (url == null)
			return null;
		
		try
		{
			// create a new AImage to read the image source
			AImage aimg = createAImage(url);
			
			if (aimg == null)
				return null;

			// create a new Image component
			org.zkoss.zul.Image img = new org.zkoss.zul.Image();
			
			// set the Image content
			img.setContent((Image) aimg);
		
			// return the new Image
			return img;
		}
		catch (Exception e)
		{
			
		}
		
		return null;
	}
	
	/**
	 * Creates an AImage, using the given URL 
	 * @param sImgSrc The relative path to the image source
	 * @return The newly created Image
	 */
	public static AImage createAImage(URL url)
	{
		if (url == null)
			return null;
		
		try
		{
			// create a new AImage to read the image source
			AImage aimg = new AImage(url);
			
			// return the new Image
			return aimg;
		}
		catch (Exception e)
		{
			
		}
		
		return null;
	}
	
	
	/**
	 * Creates an Image component, using the given 
	 * application relative path
	 * @param sImgSrc The relative path to the image source
	 * @return The newly created Image
	 */
	public static String[] getComponentEvents(Class clazz)
	{
		// get the active designer instance
		Designer designer = Designer.getCurrent();
		
		if ((designer == null) || (clazz == null))
			return null;
		
		// get the canonical name of the component's class
		String sClassName = clazz.getName();
		
		// get the component's Configurator instance
		// from the toolkit object
		Configurator config = DesignerToolkit.getComponentsConfigurator();
		
		if (config == null)
			return null;
		
		// try to locate the component element within
		// the configuration iDOM first
		Element domClass = config.getElement("class", sClassName, null); 
		
		if (domClass == null)
			return null;
		
		// if the <class> element was found, get 
		// its parent which is the <component> element
		Element domComponent = (Element) domClass.getParent();
		
		if (domComponent == null)
			return null;
		
		// get the required <events> tag from the node
		Element domImage = config.getElement("events", domComponent);
		
		if (domImage == null)
			return null;
		
		// get the component's event list
		String sEventList = domImage.getText();
		
		if (StringUtils.isEmpty(sEventList))
			return null;
		
		// split the event list into the array
		// of events
		String[] arrEvents = StringUtils.split(sEventList, ',');
	
		// return the events array
		return arrEvents;
	}
	
	/**
	 * Returns the absoulute path to a resource
	 * @param path
	 * @return
	 */
	public static URL getResourceRelativePath(String sPath)
	{
		if (StringUtils.isEmpty(sPath))
			return null;

		// get the URL of the given relative path 
		URL url = Executions.getCurrent().getDesktop().getWebApp().getResource(sPath);
		
		return url;
	}
	
	/**
	 * Walks through the given component model and 
	 * assigns new Ids to all the contained elements.
	 * @param element
	 * @return
	 */
	public static Component assignNewIds(Component element)
	{
		if (element == null)
			return null;
		
		// assign a new Id to the component
		assignNewId(element);
		
		return element;
	}
	
	/**
	 * Parses the model and converts it into the
	 * ZUML corresponding representation, by 
	 * converting each model element in turn.
	 * @param cmpRoot
	 */
	protected static void assignNewId(Component component)
	{
		if (component == null)
			return;
		
		// assign a new Id to the given component
		component.setId(createNewId());
		
		// get the component's children
		List listChildren = component.getChildren();

		if (listChildren.size() == 0)
			return;
		
		// loop through all the component's children
		Iterator iter = listChildren.iterator();
		while (iter.hasNext())
		{
			try
			{
				// get the next component in the list
				Component child = (Component) iter.next();
				
				if (child == null)
					continue;
				
				/*** RECURSION ***/
				assignNewId(child);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Creates a new auto-generated Id directly
	 * from the framework.
	 * @return The new Id
	 */
	public static String createNewId()
	{
		// get the next available component Id for the current Desktop	
		String sNewId = ((DesktopCtrl) Executions.getCurrent().getDesktop()).getNextUuid(); 
		
		// fix the Id
		sNewId = fixAutoId(sNewId);
		
		// return the new Id
		return sNewId;
	}
	
	/**
	 * Converts an auto-generated component 
	 * Id assigned from the framework into
	 * a valid one for later re-loading.
	 * @param sId The component's Id
	 * @return A new valid Id
	 */
	public static String fixAutoId(String sId)
	{
		if (StringUtils.isEmpty(sId))
			return "";
		
		// if the given Id is not auto-generated, 
		// do not change it
		if (! Components.isAutoId(sId))
			return sId;
	
		String sNewId = "";
		
		// the easiest way to convert an auto-generated
		// Id into a new valid one, is by removing the
		// leading '_' char and using a different one.
		sNewId = StringUtils.removeStart(sId, "z_");
		sNewId = "#" + sNewId;		
		
		// return the fixed Id
		return sNewId;
	}
	
	/**
	 * Parses the given component model and disposes
	 * all components that implement the IDisposable
	 * interface. 
	 * @param cmpRoot The component to dispose
	 */
	public static void disposeComponent(Component component)
	{
		if (component == null)
			return;

		// if the component is disposable, dispose
		// its resources now
		if (component instanceof IDisposable)
			((IDisposable) component).dispose();
		
		// get the component's children
		List listChildren = component.getChildren();

		if (listChildren.size() == 0)
			return;
		
		// loop through all the component's children
		Iterator iter = listChildren.iterator();
		while (iter.hasNext())
		{
			try
			{
				// get the next component in the list
				Component child = (Component) iter.next();
				
				if (child == null)
					continue;
				
				/*** RECURSION ***/
				disposeComponent(child);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	/**
	 * Returns the simple name of a component's class.
	 * @param clazz The component class to resolve
	 */
	public static String getSimpleClassName(Component cmp)
	{
		if (cmp == null)
			return "";
		
		// get the component's implementation class
		Class clazz = cmp.getClass();
		
		if (clazz == null)
			return "";
		
		// get the full class name
		String sName = clazz.getName();
		
		// split the class name at the '.' chars
		String[] arr = StringUtils.split(sName, '.');
		
		if (ArrayUtils.isEmpty(arr))
			return "";
		
		// return the last array cell
		// which is the simple name of the class
		return arr[arr.length - 1];
	}
	
	/**
	 * Returns all the valid properties of the component
	 * that should be displayed on the Property view.
	 * @param cmp The component to resolve
	 * @return A PropertyDescriptor array
	 */
	public static PropertyDescriptor[] getComponentProperties(Component cmp)
	{
		if (cmp == null)
			return null;
		
		// get the propery descriptors of the Component class
		PropertyDescriptor[] arrDescriptors = PropertyUtils.getPropertyDescriptors(cmp);	
		
		if (ArrayUtils.isEmpty(arrDescriptors))
			return null;
		
		// get the list of the component's properties
		// that should not be displayed onto the view
		String[] arrExcludedProps = RulesEngine.getComponentAttributes(cmp, RulesEngine.ATTRIBUTES_EXCLUDE_FROM_PROPERTY_VIEW);

		PropertyDescriptor[] arrValidProps = null;
		
		// loop through the component property descriptors
		for (int i = 0; i < arrDescriptors.length; i++)
		{
			try
			{
				// get the next property descriptor
				PropertyDescriptor descriptor = arrDescriptors[i];
				
				if (descriptor == null)
					continue;
				
				// get the read / write property methods
				Method methodRead = descriptor.getReadMethod();
				Method methodWrite = descriptor.getWriteMethod();
				
				// for all properties, both read / write
				// methods should exist
				if ((methodRead == null) ||
					(methodWrite == null))
					continue;
				
				// get the name of the object property
				String sName = descriptor.getName();
				
				// check the list of excluded props to see
				// if this property is banned by the rules
				if (! ArrayUtils.isEmpty(arrExcludedProps))
				{
					// if yes, move on to the next property
					if (ArrayUtils.contains(arrExcludedProps, sName))
						continue;
				}
				
				// Filter out the following properties, 
				// as they are used internally:
				//
				//  * childable
				//  * zIndexByClient
				//  * innerAttrs
				//  * leftByClient
				//  * topByClient
				//  * outerAttrs
				//  * transparent
				if (sName.equalsIgnoreCase("childable") || 
					sName.equalsIgnoreCase("zIndexByClient") || 
					sName.equalsIgnoreCase("innerAttrs") ||
					sName.equalsIgnoreCase("leftByClient") ||
					sName.equalsIgnoreCase("topByClient") ||
					sName.equalsIgnoreCase("outerAttrs") ||
					sName.equalsIgnoreCase("transparent"))
					continue;
				
				// Display only properties of the following types
				//
				// * String
				// * String[]
				// * int
				// * boolean
				// * long
				if (((descriptor.getPropertyType() != String.class) && 
					 (descriptor.getPropertyType() != String[].class) &&	
					 (descriptor.getPropertyType() != boolean.class) && 
					 (descriptor.getPropertyType() != int.class) &&
					 (descriptor.getPropertyType() != long.class)) ||
					(descriptor.isHidden())
					)	
				{	
					continue;
				}
				
				if (arrValidProps == null)
					arrValidProps = new PropertyDescriptor[]{};

				// this is a valid property, so add it to the array
				arrValidProps = (PropertyDescriptor[]) ArrayUtils.add(arrValidProps, descriptor);
			}
			catch (Exception e)
			{
				continue;
			}
		}

		// dispose
		arrDescriptors = null;
		
		// return the array of valid properties
		return arrValidProps;
	}
	
}
