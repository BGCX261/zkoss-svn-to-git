package org.zerokode.designer.model.rules;

import org.zerokode.designer.model.rules.engine.IRulable;
import org.zerokode.designer.model.rules.engine.RulesResult;
import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;


public class TabboxRules implements IRulable
{
	/* (non-Javadoc)
	 * @see com.zk.designer.model.rules.engine.IRulable#applyPreCreationRules()
	 */
	public RulesResult applyPreCreationRules() throws RulesException
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.zk.designer.ui.model.rules.engine.IRulable#applyCreationRules(com.potix.zk.ui.Component)
	 */
	public RulesResult applyCreationRules(Component comp) throws RulesException
	{
		return null;
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
		return new String[] {"selectedIndex", "selectedPanel", "selectedTab"};
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
	 * @see com.zk.designer.model.rules.engine.IRulable#applyPreCreationRules(com.potix.zk.ui.Component)
	 */
	public boolean applyPreCreationRules(Component cmp)
	{
		return true;
	}

	public RulesResult applyCopyRules(Component source) throws RulesException
	{
		return null;
	}

	public RulesResult applyPrePasteRules(Component source,
			Component target) throws RulesException
	{
		return null;
	}

}

