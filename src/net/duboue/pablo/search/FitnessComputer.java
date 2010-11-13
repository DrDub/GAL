/*
 * LearnFSA - An implementation of the Genetic Automata Learner
 * Copyright (C) 2010 Pablo Ariel Duboue <pablo.duboue@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 
 * 02110-1301 USA
 */

package net.duboue.pablo.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract class for fitness computers.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public abstract class FitnessComputer implements Runnable {
	protected static boolean mustStop = false;

	public static void stopComputers() {
		mustStop = true;
	}

	protected List<ComputationListener> listeners;
	protected int lastID;
	protected List<Task> tasks;

	public FitnessComputer() {
		this.listeners = new LinkedList<ComputationListener>();
		this.tasks = new LinkedList<Task>();
		this.lastID = 0;
	}

	public synchronized void addComputationListener(ComputationListener listener) {
		this.listeners.add(listener);
	}

	public synchronized void removeComputationListener(
			ComputationListener listener) {
		this.listeners.remove(listener);
	}

	public synchronized Object createID() {
		Object result = new Integer(lastID);
		lastID++;
		return result;
	}

	public synchronized void scheduleCompute(Object id, Instance instance) {
		synchronized (tasks) {
			tasks.add(new Task(id, instance));
			tasks.notify();
		}
	}

	protected class Task {
		public Object id;
		public Instance instance;

		public Task(Object id, Instance instance) {
			this.id = id;
			this.instance = instance;
		}
	}

	public void run() {
		Task current = null;
		while (!mustStop) {
			synchronized (tasks) {
				if (tasks.isEmpty()) {
					try {
						tasks.wait();
					} catch (InterruptedException e) {
					}
				} else
					current = tasks.remove(0);
			}
			if (current != null) {
				double fitness = compute(current.instance);
				synchronized (this) {
					Iterator<ComputationListener> l = listeners.iterator();
					while (l.hasNext())
						l.next()
								.computationPerformed(new ComputationEvent(
										current.id, new Double(fitness)));
				}
				current = null;
			}
		}
	}

	protected abstract double compute(Instance instance);
}
