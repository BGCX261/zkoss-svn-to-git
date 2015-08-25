package org.zerokode.designer.ui;



import org.zkforge.dojo.Fisheyeitem;
import org.zkforge.dojo.Fisheyelist;
import org.zerokode.designer.events.listeners.ToolbarEventListener;
import org.zkoss.zul.Caption;



/**
 * Class that implements the designer's Toolbar
 * component.
 * @author chris.spiliotopoulos
 *
 */
public class DesignerToolbar extends DesignerGroupbox
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7392542769338385183L;

	/**
	 * @param designer
	 */
	DesignerToolbar(Designer designer)
	{
		super(designer);
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.ui.DesignerWindow#onCreate()
	 */
	public void onCreate()
	{
		// create the main designer window
		setId("designerToolbar");
		setWidth("400px");
		setMold("3d");
		
		// create the Caption
		Caption caption = new Caption();
		caption.setLabel("Toolbar");
		appendChild(caption);

		Fisheyelist fishEye = new Fisheyelist();
		fishEye.setId("fishEye");
		fishEye.setAttachEdge("top");
		
		Fisheyeitem fishItem1 = new Fisheyeitem();
		fishItem1.setId("fiClearCanvas");
		fishItem1.setImage("/images/designer/toolbar/new_canvas.png");
		fishItem1.setLabel("New Canvas Window");
		fishItem1.addEventListener("onClick", new ToolbarEventListener());
		
		Fisheyeitem fishItem2 = new Fisheyeitem();
		fishItem2.setId("fiLoadPageDefinition");
		fishItem2.setImage("/images/designer/toolbar/load_page.png");
		fishItem2.setLabel("Load Page Definition");
		fishItem2.addEventListener("onClick", new ToolbarEventListener());
		
		Fisheyeitem fishItem3 = new Fisheyeitem();
		fishItem3.setId("fiSavePageDefinition");
		fishItem3.setImage("/images/designer/toolbar/save_page.png");
		fishItem3.setLabel("Save Page Definition");
		fishItem3.addEventListener("onClick", new ToolbarEventListener());
		
		Fisheyeitem fishItem4 = new Fisheyeitem();
		fishItem4.setId("fiViewPage");
		fishItem4.setImage("/images/designer/toolbar/view_page.png");
		fishItem4.setLabel("View Page In New Window");
		fishItem4.addEventListener("onClick", new ToolbarEventListener());
		
		Fisheyeitem fishItem5 = new Fisheyeitem();
		fishItem5.setId("fiInfo");
		fishItem5.setImage("/images/designer/toolbar/about.png");
		fishItem5.setLabel("Information");
		fishItem5.addEventListener("onClick", new ToolbarEventListener());
		
		fishEye.appendChild(fishItem1);
		fishEye.appendChild(fishItem2);
		fishEye.appendChild(fishItem3);
		fishEye.appendChild(fishItem4);
		fishEye.appendChild(fishItem5);
		
		appendChild(fishEye);
		
	}
	
}
