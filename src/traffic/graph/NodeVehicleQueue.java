/*******************************************************************************
 * Copyright (c) 2011 - Jochen Wuttke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jochen Wuttke (wuttkej@gmail.com) - initial API and implementation
 ********************************************************************************
 *
 * Created: Dec 13, 2011
 */

package traffic.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import traffic.model.Car;

/**
 * The {@link NodeVehicleQueue} handles traffic on a single node.
 * Entering cars are assigned to a slot matching the current delay
 * of the node, and in each simulation cycle, they are moved
 * forward one slot.
 * When they reach the end, they are asked to leave or they are stopped.
 * 
 * @author Jochen Wuttke - wuttkej@gmail.com
 *
 */
final class NodeVehicleQueue {
	
	/**
	 * ID for the special queue tracking parked/stopped vehicles.
	 */
	private final static int PARKED = -1;
	private Map<Integer, Set<Car>> queue;
	
	/**
	 * Initializes an empty queue.
	 */
	public NodeVehicleQueue() {
		queue = new HashMap<Integer, Set<Car>>();
		queue.put( PARKED, new HashSet<Car>() );
		queue.put( 0, new HashSet<Car>() );
	}
	
	/**
	 * Adds <code>c</code> to the queue in the slot matching the given <code>delay</code>.
	 * @param c
	 * @param delay
	 */
	void enqueue( Car c, int delay ) {
		assert delay >= 0;
		ensureBucketExists( delay );
		queue.get( delay ).add(c);
	}

	/**
	 * @param delay
	 */
	private void ensureBucketExists(int delay) {
		if ( queue.get(delay) == null ) {
			queue.put(delay, new HashSet<Car>() );
		}
	}

	/**
	 * Moves all cars one step ahead
	 * @return the list of cars that have reached the tip of the queue
	 */
	Set<Car> moveCars() {
		Set<Car> fs = queue.remove(0);
		SortedSet<Integer> keys = new TreeSet<Integer>( queue.keySet() );
		for ( Integer key : keys ) {
			if ( key == PARKED ) continue;	//default cases handled separately
			Set<Car> c = queue.remove(key);
			queue.put(key-1, c );
		}
		return fs;
	}
	
	/**
	 * Removes <code>c</code> from the active list of vehicles.
	 * @param c
	 */
	void park( Car c ) {
		removeFromQueue( c );
		queue.get( PARKED ).add( c );
	}

	/**
	 * @param c
	 */
	private void removeFromQueue(Car c) {
		for ( Integer key : queue.keySet() ) {
			if ( key == PARKED ) continue;
			if ( queue.get(key).remove(c) ) return;	//short circuit
		}
	}
	
	/**
	 * @return <code>true</code> if there are no cars in the queue that are waiting to move
	 */
	boolean isEmpty() {
		for ( Integer key : queue.keySet() ) {
			if ( key == PARKED ) continue;
			if ( !queue.get(key).isEmpty() ) return false; //short circuit
		}
		return true;
	}

	/**
	 * @return the current number of active cars in this queue (parked vehicles are ignored)
	 */
	public int size() {
		int s = 0;
		for ( int key : queue.keySet() ) {
			if ( key == PARKED ) continue;
			s+= queue.get(key).size();
		}	
		return s;
	}
}