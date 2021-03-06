package com.hexagone.delivery.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.hexagone.delivery.algo.DeliveryComputer;
import com.hexagone.delivery.models.ArrivalPoint;
import com.hexagone.delivery.models.Delivery;
import com.hexagone.delivery.models.DeliveryQuery;
import com.hexagone.delivery.models.Intersection;
import com.hexagone.delivery.models.Map;
import com.hexagone.delivery.models.Road;
import com.hexagone.delivery.models.RouteHelper;
import com.hexagone.delivery.models.Warehouse;
import com.hexagone.delivery.ui.MainFrame;
import com.hexagone.delivery.ui.Popup;
import com.hexagone.delivery.xml.NoFileChosenException;
import com.hexagone.delivery.xml.XMLDeserialiser;
import com.hexagone.delivery.xml.XMLException;

/**
 * This class allows us to draw the map and the points of the delivery on top of
 * it when the state is NAVIGATE_STATE
 */
public class NavigateState implements ControllerActions {

	private MainFrame frame;

	private int step;

	/**
	 * Opens a FileChooser that lets the user pick an XML file on the file
	 * system.
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

	/*
	 * (non-Javadoc)
	 * @see com.hexagone.delivery.control.ControllerActions#computeDelivery(com.hexagone.delivery.models.Map, com.hexagone.delivery.models.DeliveryQuery)
	 */
	@Override
    public RouteHelper computeDelivery(Map map, DeliveryQuery delivery) {
        DeliveryComputer computer = new DeliveryComputer(map, delivery);
        computer.compute();

        if (!computer.checkNotEmptySolution()){
        	if (computer.checkTimeout()){
        		Popup.showInformation("Aucune solution n'a été trouvée \ndans le temps imparti", "Temps de calcul écoulé");
        		return null;
        	} else {
        		Popup.showInformation("Il n'existe aucune tournée satisfaisante.", "Tournée impossible");
        		return null;
        	}
        } else if (computer.checkTimeout()) {
        	Popup.showInformation("La tournée calculée n'est peut-être pas optimale.", "Temps de calcul écoulé");
        }
        computer.getDeliveryPoints();
        return new RouteHelper(map, delivery, computer);
    }

	/*
	 * (non-Javadoc)
	 * @see com.hexagone.delivery.control.ControllerActions#generatePlanning(com.hexagone.delivery.models.RouteHelper)
	 */
	@Override
	public void generatePlanning(RouteHelper routeHelper) {
		routeHelper.writeToTxt("export/planning.txt");		
		Popup.showInformationWithScroll(routeHelper.getPlanning(), "Feuille de route généré !");
	}

	/*
	 * (non-Javadoc)
	 * @see com.hexagone.delivery.control.ControllerActions#DrawMap(java.awt.Graphics, float, com.hexagone.delivery.models.Map, com.hexagone.delivery.models.DeliveryQuery, com.hexagone.delivery.models.RouteHelper)
	 */
	@Override
	public void DrawMap(Graphics g, float coefficient, Map map, DeliveryQuery deliveryQuery, RouteHelper routeHelper) {
		ArrayList<Intersection> intersections = new ArrayList<Intersection>(map.getIntersections().values());
		Set<Integer> roads = new HashSet<Integer>();
		roads = (map.getRoads()).keySet();

		for (int j : roads) {
			ArrayList<Road> roadsFromI = new ArrayList<Road>();
			roadsFromI = map.getRoads().get(j);
			for (Road r : roadsFromI) {
				g.setColor(Color.GRAY);
				Graphics2D g2 = (Graphics2D) g;
				Point destination = intersections.get(r.getOrigin()).getCoordinates();
                Point origine = intersections.get(r.getDestination()).getCoordinates();
				Line2D lin = new Line2D.Float(((origine.x) / coefficient) + 5, ((origine.y) / coefficient) + 5,
						((destination.x) / coefficient) + 5, ((destination.y) / coefficient) + 5);
				g2.draw(lin);
			}
		}

		Warehouse warehouse = deliveryQuery.getWarehouse();
		Intersection intersectionWarehouse = warehouse.getIntersection();
		Point pointWarehouse = intersections.get(intersectionWarehouse.getId()).getCoordinates();

		HashMap<Integer, ArrivalPoint> tour = routeHelper.getRoute();
		Set<Entry<Integer, ArrivalPoint>> entrySet = tour.entrySet();
		Iterator<Entry<Integer, ArrivalPoint>> iterator = entrySet.iterator();
		for (int i = 0; i < step + 1 && i < tour.size(); i++) {

			Entry<Integer, ArrivalPoint> entry = iterator.next();
			Point pointDelivery = new Point();

			// Drawing the entire path
			for (Entry<Integer, ArrivalPoint> entryALLMAP : tour.entrySet()) {

				ArrayList<Road> roadsToNextDP = entryALLMAP.getValue().getRoads();
				for (Road r : roadsToNextDP) {
					Graphics2D g2 = (Graphics2D) g;
					Point destination = intersections.get(r.getOrigin()).getCoordinates();
	                Point origine = intersections.get(r.getDestination()).getCoordinates();
					Line2D lin = new Line2D.Float(((origine.x) / coefficient) + 5, ((origine.y) / coefficient) + 5,
							((destination.x) / coefficient) + 5, ((destination.y) / coefficient) + 5);
					g2.setColor(Color.GREEN);
					g2.setStroke(new BasicStroke(3));
					g2.draw(lin);
				}

			}

			// Drawing the path to wards the 'step' delivery
			ArrayList<Road> roadsToNextDP = entry.getValue().getRoads();
			for (Road r : roadsToNextDP) {
				g.setColor(Color.BLACK);
				Graphics2D g2 = (Graphics2D) g;
				Point destination = intersections.get(r.getOrigin()).getCoordinates();
                Point origine = intersections.get(r.getDestination()).getCoordinates();
				Line2D lin = new Line2D.Float(((origine.x) / coefficient) + 5, ((origine.y) / coefficient) + 5,
						((destination.x) / coefficient) + 5, ((destination.y) / coefficient) + 5);
				g2.setStroke(new BasicStroke(3));
				g2.draw(lin);
			}
			if (i == step) {
				// g.setColor(new Color(0, 102, 0));
				g.setColor(Color.BLACK);
				pointDelivery = intersections.get(entry.getKey()).getCoordinates();
				g.fillOval((int) (((pointDelivery.x)) / coefficient) - 5, (int) (((pointDelivery.y)) / coefficient) - 5,
						20, 20);
			}
		}

		// Draw Delivery points
		for (Intersection in : intersections) {
			Point p = new Point();
			p = in.getCoordinates();
			g.setColor(Color.BLUE);
			g.fillOval((int) (((p.x)) / coefficient), (int) (((p.y)) / coefficient), 10, 10);
		}

		Delivery[] deliveries = deliveryQuery.getDeliveries();
		g.setColor(new Color(20, 200, 20));
		Point pointDelivery;
		for (Delivery d : deliveries) {
			pointDelivery = intersections.get(d.getIntersection().getId()).getCoordinates();
			g.fillOval((int) (((pointDelivery.x)) / coefficient) - 1, (int) (((pointDelivery.y)) / coefficient) - 1, 12,
					12);
		}
		g.setColor(Color.RED);
		g.fillOval((int) (((pointWarehouse.x)) / coefficient) - 2, (int) (((pointWarehouse.y)) / coefficient) - 2, 14,
				14);
		g.drawString("Entrepôt", (int) (((pointWarehouse.x)) / coefficient + 5),
				(int) (((pointWarehouse.y)) / coefficient));

	}

	/**
	 * Repaints the frame when the tour starts
	 */
	public void startTour() {
		step = 0;
		frame.repaint();
	}

	/**
	 * Increments the step and moves to the next delivery
	 * 
	 * @param maxValue
	 *            : maximum value for the step
	 */
	public void nextDelivery(int maxValue) {
		step++;
		if (step > maxValue) {
			step = maxValue;
		}
		frame.selectionRow(step);
		frame.repaint();
	}

	/**
	 * Decrements the step if it is positive
	 */

	public void previousDelivery() {
		if (step > 0) {
			step--;
		}

		frame.selectionRow(step);
		frame.repaint();
		
	}

	/**
	 * Constructor
	 * @param frame the mainFrame the controller controls.
	 */
	public NavigateState(MainFrame frame) {
		this.frame = frame;
	}

	public void searchDPByID(int idP) {
		frame.selectionRow(idP);
		frame.repaint();
		
	}

	public int getRowSelected() {
		return frame.getRowSelected();
		
	}

}
