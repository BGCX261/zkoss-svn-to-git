package org.zerokode.designer.model.rules;


import org.zerokode.designer.model.rules.engine.IRulable;
import org.zerokode.designer.model.rules.engine.RulesResult;
import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Treechildren;


/**
 * 
 * @author chris.spiliotopoulos
 *
 */
public class TreeRules implements IRulable
{
	/* (non-Javadoc)
	 * @see com.zk.designer.ui.model.rules.IRulable#applyCreationRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyCreationRules(Component cmp) throws RulesException
	{
		if (cmp == null)
			return null;
		
		try
		{
			// initialy a Tree component must
			// have a Treechildren collection attached
			Treechildren children = new Treechildren();
			cmp.appendChild(children);
		}
		catch (Exception e)
		{
			throw new RulesException(new RulesResult(RulesResult.ERR_UNSPECIFIED, e.getMessage()));
		}
			
		// return a success result
		return new RulesResult(RulesResult.SUCCESS, "");
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.ui.model.rules.IRulable#applyModelToZUMLRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyModelToZUMLRules(Component cmp) throws RulesException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.ui.model.rules.engine.IRulable#getModelToZUMLExcludedProperties()
	 */
	public String[] getModelToZUMLExcludedAttributes()
	{
		return new String[] {"selectedItem"};
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#getExcludedProperties()
	 */
	public String[] getExcludedProperties()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyComponentDisplayRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyComponentDisplayRules(Component cmp)
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
	public RulesResult applyPreCreationRules() throws RulesException
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
