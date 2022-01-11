package com.ensoftcorp.demo;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ensoftcorp.atlas.ui.AtlasUI;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.views.scriptView.IAtlasScriptView;

import UIScript.defaultUiScript;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "AtlasAPIDemo"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public static IAtlasScriptView RGUI ;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	/**
	 * Starts plugin and Refactoring guidance view 
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		
				
		
		//for some reason this cant run on SWT/ui thread
		Thread t1 = new Thread(
						
						new Runnable(){
							@Override
							public void run() {
								
								try {
									
									RGUI = AtlasUI.createScriptView();
									AtlasSmartViewScript s = new defaultUiScript();
									RGUI.script(s);
									RGUI.title("Refactoring Guidance");
									
								} catch (PartInitException e) {
									// 
									e.printStackTrace();
								} 
		
								
								
								
							}
						}
						
						);
        t1.start();
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
