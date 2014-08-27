/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asu.mwdb.phase3.task6;

/**
 *
 * @author paddy
 */
public class MwdbPhase3Task6 {

    public String queryGesturePath ="";
    
    public RelevanceBasedDecisionTreeImplUI relevanceBasedDecisionTreeImplUI ;
    
    
    public MwdbPhase3Task6( RelevanceBasedDecisionTreeImplUI relevanceBasedDecisionTreeImplUI){
    	
    	this.relevanceBasedDecisionTreeImplUI = relevanceBasedDecisionTreeImplUI;
    	
    	try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(
					RelevanceFeedbackUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(
					RelevanceFeedbackUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(
					RelevanceFeedbackUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(
					RelevanceFeedbackUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
    	
		new RelevanceFeedbackUI(relevanceBasedDecisionTreeImplUI).setVisible(true);
    }
}
