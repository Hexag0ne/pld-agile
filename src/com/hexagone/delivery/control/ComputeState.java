package com.hexagone.delivery.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import com.hexagone.delivery.algo.DeliveryComputer;
import com.hexagone.delivery.models.Delivery;
import com.hexagone.delivery.models.DeliveryQuery;
import com.hexagone.delivery.models.Intersection;
import com.hexagone.delivery.models.Map;
import com.hexagone.delivery.models.Road;
import com.hexagone.delivery.models.RouteHelper;
import com.hexagone.delivery.models.Warehouse;
import com.hexagone.delivery.ui.Popup;
import com.hexagone.delivery.xml.NoFileChosenException;
import com.hexagone.delivery.xml.XMLDeserialiser;
import com.hexagone.delivery.xml.XMLException;

/**
 * This class allows us to draw the map and the points of the delivery on top of it
 * when the state is COMPUTE_STATE
 */
public class ComputeState implements ControllerActions {

	/**
	 * Opens a FileChooser that lets the user pick an XML file on the file system.
	 */
	@Override
	public Map loadMap() {
		try {
			return XMLDeserialiser.loadMap();
		} catch (XMLException e) {
			Popup.showInformation("Le fichier choisi n'est pas un plan valide.");
			return null;
		} catch (NoFileChosenException e) {
			return null;
		}
	}

	/**
	 * This method allows to load a delivery query from a XML file
	 */
	@Override
	public DeliveryQuery loadDeliveryQuery() {
		try {
			return XMLDeserialiser.loadDeliveryQuery();
		} catch (XMLException e) {
			Popup.showInformation("Le fichier choisi n'est pas une livraison valide.");
			return null;
		} catch (NoFileChosenException e) {
			return null;
		}
	}

	/**
	 * This method computes a delivery and returns a Route   
	 * @param map
	 * @param delivery
	 * @return the route computed as a Route Object
	 * 
	 */
	@Override
	public RouteHelper computeDelivery(Map map, DeliveryQuery delivery) {
		DeliveryComputer computer = new DeliveryComputer(map, delivery);
		computer.getDeliveryPoints(); // to launch computation
		
		return new RouteHelper(map, delivery, computer);
	}
	
	@Override
	public void generatePlanning(RouteHelper routeHelper) {
		JOptionPane.showMessageDialog(null, "Veuillez calculez la tournée.", "Erreur", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This methods draws the map and the points of the delivery on top of it
	 * (as the map and the deliveryQuery are known in the class). 
	 * @param g 
	 * @param scale 
	 * 			: ratio chosen for the drawing of the map
	 * @param map
	 * @param deliveryQuery
	 * @param route
	 */
	@Override
	public void DrawMap(Graphics g, float scale, Map map, DeliveryQuery deliveryQuery, RouteHelper routeHelper) {
		
		//Painting the map
		//Painting the roads first
		ArrayList<Intersection> intersections = new ArrayList<Intersection>(map.getIntersections().values());
		Set<Integer> roads = new HashSet<Integer>();
		roads = (map.getRoads()).keySet();

		for (int j : roads) {
			ArrayList<Road> roadsFromI = new ArrayList<Road>();
			roadsFromI = map.getRoads().get(j);
			for (Road r : roadsFromI) {
				g.setColor(Color.BLACK);
				Graphics2D g2 = (Graphics2D) g;
				Point destination = null;
				Point origine = null;
				for (Intersection in : intersections) {
					if ((in.getId()).equals(r.getOrigin())) {
						origine = in.getCoordinates();
						break;
					}
				}
				for (Intersection in : intersections) {
					if ((in.getId()).equals(r.getDestination())) {
						destination = in.getCoordinates();
						break;
					}
				}
				Line2D lin = new Line2D.Float(((origine.x) / scale) + 5, ((origine.y) / scale) + 5,
						((destination.x) / scale) + 5, ((destination.y) / scale) + 5);
				g2.setStroke(new BasicStroke(2));
				g2.draw(lin);
			}
		}
		//Painting the  of the map
		for (Intersection i : intersections) {
			Point p = new Point();
			p = i.getCoordinates();
			g.setColor(Color.BLUE);
			g.fillOval((int)(((p.x)) / scale),(int) (((p.y)) / scale), 10, 10);
		}
		
		//Drawing the deliveryQuery
		Warehouse warehouse = deliveryQuery.getWarehouse();
		Delivery[] deliveries = deliveryQuery.getDeliveries();

		Intersection intersectionWarehouse = warehouse.getIntersection();
		Point pointWarehouse = new Point();
		for (Intersection in : intersections) {
			if ((in.getId()).equals(intersectionWarehouse.getId())) {
				pointWarehouse = in.getCoordinates();
				break;
			}
		}
		// Draw Warehouse
		g.setColor(Color.RED);
		g.fillOval((int)(((pointWarehouse.x)) / scale)-2,(int)( ((pointWarehouse.y)) / scale)-2, 14, 14);
		g.drawString("Entrepôt",(int)( ((pointWarehouse.x)) / scale + 5), (int)(((pointWarehouse.y)) / scale));

		// Draw Delivery points
		g.setColor(new Color(20, 200, 20));
		for (Delivery d : deliveries) {
			Intersection i = d.getIntersection();
			Point pointDelivery = new Point();
			for (Intersection in : intersections) {
				if ((in.getId()).equals(i.getId())) {
					pointDelivery = in.getCoordinates();
					break;
				}
			}
			g.fillOval((int)(((pointDelivery.x)) / scale)-1,(int)( ((pointDelivery.y)) / scale)-1, 12, 12);

		}
	}

}
