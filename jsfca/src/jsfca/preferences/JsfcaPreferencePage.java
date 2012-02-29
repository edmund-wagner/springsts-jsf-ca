package jsfca.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import jsfca.Activator;

/**
 * Jsf CA additional suggestions pref page
 * 
 * @author ewagner
 * 
 */
public class JsfcaPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public JsfcaPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Jsf Content Assist - Additional Suggestions as beans");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(
				JsfcaPerferenceConstants.PROP_KEY_BASE_PACKAGE,
				"&Base package:", getFieldEditorParent()));
		addField(new StringFieldEditor(
				JsfcaPerferenceConstants.PROP_KEY_FILE_PATTERN,
				"&Class regex:", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}