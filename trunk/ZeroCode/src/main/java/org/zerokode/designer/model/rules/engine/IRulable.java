package org.zerokode.designer.model.rules.engine;

import org.zerokode.designer.model.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;


/**
 * Interface defining a rulable object, which
 * is an object that is used by the rules engine.
 *  
 * @author chris.spiliotopoulos
 *
 */
public interface IRulable
{
	/**
	 * This method is called just before a visual component
	 * is created by the designer. If the resulting value
	 * is <b>false</b>, the component is not created.
	 */
	public RulesResult applyPreCreationRules() throws RulesException;
	
	/**
	 * This method is called right after a visual component
	 * is created by the designer.  This happens when the user
	 * adds a new component onto the canvas.
	 * @param comp The newly created component
	 */
	public RulesResult applyCreationRules(Component cmp) throws RulesException;
	
	/**
	 * This method is called just before a component
	 * within the canvas model is exported to a ZUML
	 * file.
	 * @param cmp The visual component to be exported
	 */
	public RulesResult applyModelToZUMLRules(Component cmp) throws RulesException;
	
	/**
	 * This method is called just before a component
	 * gets displayed onto the model tree.
	 * @param cmp The component under resolution 
	 */
	public RulesResult applyComponentDisplayRules(Component cmp) throws RulesException;
	
	/**
	 * Called right after the user has copied a component on 
	 * the model tree
	 * @param source The source component
	 * @return A rules operation result
	 * @throws RulesException
	 */
	public RulesResult applyCopyRules(Component source) throws RulesException;
	
	/**
	 * Called just before the user pastes a component onto 
	 * another one on the model tree
	 * @param clone The cloned source component
	 * @param target The target component
	 * @return A rules operation result
	 * @throws RulesException
	 */
	public RulesResult applyPrePasteRules(Component clone, 
										  Component target) throws RulesException;
	
	/**
	 * The method returns a String array of all
	 * the component's properties that should not
	 * be displayed in the ZUL file.
	 */
	public String[] getModelToZUMLExcludedAttributes();
	
	/**
	 * The method returns a String array of all
	 * the component's properties that should not
	 * be displayed in the property view dialog.
	 */
	public String[] getExcludedProperties();
	
	
	/**
	 * Returns a boolean indicating whether the component's
	 * children should be displayed or not onto the model
	 * treeview.
	 */
	public boolean showChildren();
	
	/**
	 * Returns a boolean indicating whether the component's
	 * children should be exported or not to the ZUML
	 * representation.
	 */
	public boolean exportChildrenToZUML();
}
