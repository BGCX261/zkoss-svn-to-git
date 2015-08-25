package org.zerokode.designer.ui;

import org.apache.commons.lang.StringUtils;
import org.zerokode.designer.IDisposable;
import org.zerokode.designer.model.CanvasTreeSynchronizer;
import org.zerokode.designer.model.rules.engine.Rules;
import org.zerokode.designer.model.rules.engine.RulesEngine;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;


/**
 * Class the utilizes the main designer window.
 * 
 * @author chris.spiliotopoulos
 *
 */
public class Designer extends Window
					  implements IDisposable
{		
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 9068354798991194078L;

	/**
	 * The widget's Tree view window 
	 */
	protected DesignerTree _tree = null;
	
	/**
	 * The designer's Widget view 
	 */
	protected DesignerToolkit _toolkit = null;
	
	/**
	 * The designer's Toolbar
	 */
	protected DesignerToolbar _toolbar = null; 
		
	/**
	 * The designer canvas 
	 */
	protected DesignerCanvas _canvas = null;
	
	/**
	 * Canvas / Tree model synchronizer 
	 */
	protected CanvasTreeSynchronizer _sync = null;
	
	/**
	 * The Web application object
	 */
	protected WebApp _webApp = null;
	
	/**
	 * The filename where the design was loaded from
	 */
	private String _sSourceFilename = "";
	
	/**
	 * The filename the design will be saved to 
	 */
	private String _sTargetFilename = "";
	
	/**
	 * Flag indicating whether the canvas holds 
	 * an empty model 
	 */
	private boolean _bIsDesignNew = true;
	
	/**
	 * The component's rules object
	 */
	private static Rules _rules = null;
	
	/** Stores the current {@link Execution}. */
	protected static final ThreadLocal _exec = new ThreadLocal();

	// Getters / Setters
	public CanvasTreeSynchronizer getSynchronizer() { return _sync; }
	public WebApp getWebApp() { return _webApp; }
	public DesignerTree getTree() { return _tree; }
	public DesignerToolbar getToolbar() { return _toolbar; }
	public DesignerToolkit getToolkit() { return _toolkit; }
	public DesignerCanvas getCanvas() { return _canvas; }
	public String getSourceFilename() { return _sSourceFilename; }
	public void setSourceFilename(String sFilename) { _sSourceFilename = sFilename; }
	public String getTargetFilename() { return _sTargetFilename; }
	public void setTargetFilename(String sFilename) { _sTargetFilename = sFilename; }
	public boolean isDesignNew() { return _bIsDesignNew; }
	
	/**
	 * Returns the Rules object that contains all
	 * the active component rules
	 */
	public static Rules getRules() { return _rules; }

	/**
	 * Returns the current Designer instance
	 */
	public static final Designer getCurrent() { return (Designer) _exec.get(); }
	
	/**
	 * Create all the designer components
	 */
	public void onCreate() 
	{ 
		// set the designer instance as a local thread 
		// variable, so it can be accessed statically
		_exec.set(this);
		
		// get the Web application object
		_webApp = Executions.getCurrent().getDesktop().getWebApp();

		// create the model synchronizer
		_sync = new CanvasTreeSynchronizer(this);
		
		// create the main designer window
		setTitle("Zero Kode by chris.spiliotopoulos [or painting with ZK]");
		setId("wndDesigner");
		setHeight("100%");
		setBorder("normal");
	
		// create the Toolbar component
		_toolbar = new DesignerToolbar(this);
		_toolbar.onCreate();

		// create the Widget view component
		_toolkit = new DesignerToolkit(this);
		_toolkit.onCreate();
		
		// create the drawing Canvas
		_canvas = new DesignerCanvas(this);
		_canvas.onCreate();
		
		// create the Tree view component
		_tree = new DesignerTree(this);
		_tree.onCreate();
		
		// layout all the window component
		layoutComponents();
		
		// load the rules applying to certain 
		// components from the 'rules.xml' configuration file
		loadRules();
		
		// clean up some memory now
		cleanUpMemory();
	}
	
	/**
	 * Creates the main desinger window
	 * @param wndDesigner
	 */
	public void layoutComponents()
	{
		// create a vertical box layout
		Vbox vBox = new Vbox();
		Vbox vbox2 = new Vbox();
		Hbox hBox = new Hbox();
		hBox.setSpacing("15px");
		vBox.setSpacing("35px");
		vbox2.setWidth("100%");
		
		appendChild(hBox);
		
		hBox.appendChild(vBox);
		hBox.appendChild(vbox2);
		hBox.setWidths("35%,60%");
		vBox.appendChild(_toolbar);
		vBox.appendChild(_toolkit);
		vBox.appendChild(_tree);
		
		vbox2.appendChild(_canvas);
	}
	
	/**
	 * Loads all the predefined rules that apply
	 * to certain components from the 'rules.xml'
	 * configuration file. 
	 */
	private void loadRules()
	{
		// get the real path of the configuration file
		String uri = _webApp.getRealPath("/config/rules/rules.xml");
		
		if (StringUtils.isEmpty(uri))
			return;
		
		// load the rules using the rules engine
		_rules = RulesEngine.loadComponentRules(uri);
		
	}
	
	/**
	 * Frees up dead memory. 
	 */
	public void cleanUpMemory()
	{
		System.out.println("Memory before clean-up: " + Runtime.getRuntime().freeMemory());

		// release any reserved memory now
		Runtime.getRuntime().gc();
		
		System.out.println("Memory after clean-up: " + Runtime.getRuntime().freeMemory());
	
		// reset the static Designer instance
		_exec.set(this);
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// dispose all objects
		if (_rules != null)
		{
			_rules.dispose();
			_rules = null;
		}
	}
}