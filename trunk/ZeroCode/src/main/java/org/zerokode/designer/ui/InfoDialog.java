package org.zerokode.designer.ui;



import org.zerokode.designer.IDisposable;
import org.zerokode.designer.model.ComponentFactory;
import org.zkoss.zul.Image;
import org.zkoss.zul.Window;


/**
 * Implements a modal dialog that gets
 * displayed each time a model is about to 
 * be saved into a file.
 * @author chris.spiliotopoulos
 *
 */
public class InfoDialog extends Window
				        implements IDisposable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6993444195214265984L;

	/**
	 * 1-argument constructor
	 * @param designer The current designer instance
	 */
	public InfoDialog()
	{

	}
	
	/**
	 * Create the dialog
	 */
	public void onCreate()
	{
		// set the window properties
		setId("dlgSaveModel");
		setTitle("The Author");
		setWidth("245px");
		setBorder("normal");
		setClosable(true);
		
		// show me
		Image img = ComponentFactory.createImage(InfoDialog.class.getName(), "images/designer/info/chris.png");
		appendChild(img);
	}
	

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// clean up
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
