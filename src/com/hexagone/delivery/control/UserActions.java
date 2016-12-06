package com.hexagone.delivery.control;

public interface UserActions {

	/**
	 * Method launched when the user clicks on the load map button
	 */
	public void loadMapButtonClick();
	
	/**
	 * Method launched when the user clicks on the load delivery Query button
	 */
	public void loadDeliveryQueryButtonClick();
	
	/**
	 * Method maunched when the user clicks on the compute route button
	 */
	public void computeRouteButtonClick();
	
	/**
	 * Method launched when the user clicks on the generate planning button 
	 */
	public void generatePlanningButtonClick();
	
	/**
	 * Method launched when the user navigates forward through the different steps of the delivery
	 */
	public void nextDelivery();
	
	/**
	 * Method launched when the user navigates backward through the different steps of the delivery
	 */
	public void previousDelivery();
}
