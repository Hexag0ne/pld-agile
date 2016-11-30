package com.hexagone.delivery.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.hexagone.delivery.models.Delivery;
import com.hexagone.delivery.models.DeliveryQuery;
import com.hexagone.delivery.models.Map;
import com.hexagone.delivery.models.Road;

/**
 * This class provides the algorithms needed to compute the complete time graph
 * between the different delivery points of a map. It delivers this information
 * as an adjacency matrix (2D array in our case) It does not perform any check
 * on the validity of the input parameters.
 */
class CompleteGraphComputer {
	/**
	 * The main goal of this class is to apply the Dijkstra alogrithm to obtain
	 * an adjacent matrix to then compute the most tume efficient way around the
	 * different passage points given in the deliveryQuery
	 */

	public static Double[][] getAdjacencyMatrix(Map map, DeliveryQuery deliveryQuery) {
		/** Creation of the adjacency matrix */
		int nbPassagePoints = deliveryQuery.getPassagePointsNumber();
		Double[][] adjacencyMatrix = new Double[nbPassagePoints][];

		Integer[] passageIntersections = deliveryQuery.getDeliveryPassageIdentifiers();

		/** We compute the cost of going to each node from each node */
		for (int i = 0; i < passageIntersections.length; i++) {
			int numberOfIntersections = map.getIntersections().size();
			HashMap<Integer, Double> cost = new HashMap<Integer, Double>(numberOfIntersections);
			HashMap<Integer, Integer> previousIntersection = new HashMap<Integer, Integer>(numberOfIntersections);

			computeCosts(map, passageIntersections[i], previousIntersection, cost);

			Double[] adjacencyLine = new Double[nbPassagePoints];
			for (int j = 0; j < nbPassagePoints; j++) {
				adjacencyLine[j] = cost.get(passageIntersections[j]);
			}
			adjacencyMatrix[i] = adjacencyLine;
			cost.clear();
			previousIntersection.clear();
		}

		/** Return */
		return adjacencyMatrix;
	}

	/**
	 * Allows to compute the costs of going from intersection 'Intersection' to
	 * all the other points in the map The result is stored in the HashMap cost.
	 * The HashMap previous stores the Intersection from which one needs to come
	 * from to go by the shortes path.
	 * 
	 * @param map
	 *            the map in which the problem takes place
	 * @param intersection
	 *            the starting intersection identifier
	 * @param previousIntersection
	 *            hashMap that will contain for each Intersection the
	 *            Intersection one needs to come from
	 * @param cost
	 *            the hashMap that will contain the costs of going from
	 *            intersection to each node
	 */
	static void computeCosts(Map map, Integer intersection, HashMap<Integer, Integer> previousIntersection,
			HashMap<Integer, Double> cost) {
		/** Set of the non-visited nodes */
		HashSet<Integer> nonVisitedNodes = map.getAllIntersectionIdentifiers();

		/** Cost array initialisation */
		cost.put(intersection, new Double(0));

		/** Beginning of the computation */
		while (!nonVisitedNodes.isEmpty()) {
			/**
			 * We select the node with the smallest 'distance' so far. In the
			 * first iteration, the origin node is selected
			 */
			intersection = smallestCost(cost, nonVisitedNodes);

			/** We visit this intersection */
			nonVisitedNodes.remove(intersection);

			/**
			 * we go through each of the available neighbours starting from the
			 * current intersection
			 */
			ArrayList<Road> neighbours = map.getRoadsStartingFrom(intersection);
			for (Road road : neighbours) {
				/**
				 * We check if the destination road is still inside the
				 * non-visited nodes
				 */
				Integer destination = road.getDestination();
				if (nonVisitedNodes.contains(destination)) {
					Double costToDestination = cost.get(intersection) + road.getTime();

					/**
					 * If we found a shorter path towards destination, we
					 * replace the cost in the cost map
					 */
					if (!cost.containsKey(destination) || cost.get(destination) > costToDestination) {
						cost.put(destination, costToDestination);
						previousIntersection.put(destination, intersection);
					}
				}
			}
		}
	}

	/**
	 * This method gives the key of the smallest element in the array
	 * 
	 * @param array
	 *            the array from whih one wants to find the minimum
	 * @return the index of the minimum element in the array as an int
	 */
	static Integer smallestCost(HashMap<Integer, Double> hashMap, HashSet<Integer> keyCandidates) {
		Iterator<Integer> keySetIterator = keyCandidates.iterator();
		Integer key = keySetIterator.next();
		Double smallestCost = hashMap.getOrDefault(key, Double.MAX_VALUE);
		while (keySetIterator.hasNext()) {
			Integer newKey = keySetIterator.next();
			Double newCost = hashMap.getOrDefault(newKey, Double.MAX_VALUE);
			if (newCost < smallestCost) {
				key = newKey;
				smallestCost = newCost;
			}
		}
		return key;
	}

}
