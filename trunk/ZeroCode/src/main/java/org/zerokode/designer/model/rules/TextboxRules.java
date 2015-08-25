package org.zerokode.designer.model.rules;


import org.zerokode.designer.model.rules.engine.IRulable;
import org.zerokode.designer.model.rules.engine.RulesResult;
import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;


/**
 * Rules for the Textbox component
 * @author chris.spiliotopoulos
 *
 */
public class TextboxRules implements IRulable
{

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyCreationRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyCreationRules(Component comp) throws RulesException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyModelToZUMLRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyModelToZUMLRules(Component cmp) throws RulesException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#getModelToZUMLExcludedAttributes()
	 */
	public String[] getModelToZUMLExcludedAttributes()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#getExcludedProperties()
	 */
	public String[] getExcludedProperties()
	{
		return new String[] {"text", "textByClient"};
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyComponentDisplayRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyComponentDisplayRules(Component cmp) throws RulesException
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#showChildren()
	 */
	public boolean showChildren()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#exportChildrenToZUML()
	 */
	public boolean exportChildrenToZUML()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyPreCreationRules()
	 */
	public RulesResult applyPreCreationRules()
	{
		return null;
	}

	public RulesResult applyCopyRules(Component source) throws RulesException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public RulesResult applyPrePasteRules(Component source,
			Component target) throws RulesException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
