package org.zerokode.designer;



/**
 * Implemented by objects that
 * take care of their own disposal
 * @author chris.spiliotopoulos
 *
 */
public interface IDisposable
{
	/**
	 * Enforces an object to clean-up itself 
	 */
	public void dispose();
}
