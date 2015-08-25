package org.zerokode.designer.model.rules.engine.exceptions;

import org.zerokode.designer.model.rules.engine.RulesResult;

/**
 * An exception thrown by the rules engine
 * when a component either cannot or should not
 * be created.
 * @author chris.spiliotopoulos
 *
 */
public class PreCreationException extends RulesException
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6999389554905012446L;

	/**
	 * Constructor
	 * @param result The rules engine result object 
	 */
	public PreCreationException(RulesResult result)
	{
		super(result);
	}

}
