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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.duboue.pablo.util.ObjectSeq;
import net.duboue.pablo.util.Randomizable;

/**
 * A genetic search class.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public class GeneticSearch implements Randomizable, ComputationListener {
	public static boolean debug = false;

	protected int populationSize;
	protected double discardPercent;
	protected double mutatePercent;
	public Instance[] instancePool; // sorted by fitness
	public boolean computing;

	public double bestFitness = Double.MIN_VALUE;
	public Instance bestInstance = null;

	public double getBestFitness() {
		return bestFitness;
	}

	public Instance getBestInstance() {
		return bestInstance;
	}

	protected transient Random rnd = new Random();

	public Random getRandom() {
		return rnd;
	}

	public void setRandom(Random rnd) {
		this.rnd = rnd;
	}

	protected transient FitnessComputer computer;

	/**
	 * On-going tasks.
	 */
	protected Map<Object, Instance> computingTasks; // key = verb_taskID,

	public GeneticSearch(FitnessComputer computer) {
		this(computer, 2000, 0.25, 0.4);
	}

	/**
	 * Initialize a GeneticSearch problem.
	 * 
	 * @param computer
	 *            the fitness computer.
	 * @param populationSize
	 *            total population size.
	 * @param discardPercent
	 *            percentage of populationSize that is discarded in each step.
	 * @param mutatePercent
	 *            percentage of the discarded instances that are replaced by
	 *            mutated instances (the rest is replaced by cross over
	 *            instances).
	 */
	public GeneticSearch(FitnessComputer computer, int populationSize,
			double discardPercent, double mutatePercent) {
		this.computer = computer;
		computer.addComputationListener(this);
		this.populationSize = populationSize;
		this.discardPercent = discardPercent;
		this.mutatePercent = mutatePercent;
		this.computingTasks = Collections
				.synchronizedMap(new HashMap<Object, Instance>());
	}

	public void startPopulation(InstanceFactory factory, int initialMutation) {
		// create the initial pool by generating them at random
		this.instancePool = new Instance[populationSize];
		for (int i = 0; i < instancePool.length; i++) {
			instancePool[i] = factory.newInstance();
			for (int j = 0; j < initialMutation; j++) {
				instancePool[i].pointMutationGrow();
				instancePool[i].pointMutationShuffle();
			}
		}

		// compute the fitness of all of them
		computeFitness(0, populationSize);

		// then sort according to fitness
		Arrays.sort(instancePool);
		ObjectSeq.reverse(instancePool);
		bestFitness = instancePool[0].getFitness();
		bestInstance = instancePool[0];
		dumpPopulation();
	}

	public void setInitialPopulation(Set<Instance> population) {
		int c = 0;
		Iterator<Instance> p = population.iterator();
		this.instancePool = new Instance[populationSize];
		while (p.hasNext()) {
			instancePool[c] = p.next();
			c++;
		}
		// sort according to fitness
		Arrays.sort(instancePool);
		ObjectSeq.reverse(instancePool);
		bestFitness = instancePool[0].getFitness();
		bestInstance = instancePool[0];
		// System.err.println(Arrays.asList(instancePool));
	}

	/**
	 * Perform a step of genetic search.
	 */
	public void step() {
		// discard less fit more likely
		int toDiscard = (int) (populationSize * discardPercent);
		int pos = 1;
		while (toDiscard > 0) {
			int discarded = rnd.nextInt(pos);
			if (instancePool[populationSize - discarded - 1] != null) {
				instancePool[populationSize - discarded - 1] = null;
				toDiscard--;
			}
			if (pos < populationSize - 2)
				pos++;
		}

		// compact the table
		int emptyPos = populationSize - 1;
		while (instancePool[emptyPos] != null)
			emptyPos--;
		for (int i = populationSize - 1; i >= 0; i--) {
			if (instancePool[i] != null && i < emptyPos) {
				instancePool[emptyPos] = instancePool[i];
				instancePool[i] = null;
				while (instancePool[emptyPos] != null)
					emptyPos--;
			}
		}

		int base = (int) (populationSize * discardPercent);
		int toFill = base - 1;
		int toMutate = (int) (toFill * mutatePercent);

		if (debug) {
			for (int i = 0; i < base; i++)
				if (instancePool[i] != null)
					System.err.println("instancePool[i] != null for i=" + i);
			for (int i = base; i < populationSize; i++)
				if (instancePool[i] == null)
					System.err.println("instancePool[i] == null for i=" + i);
		}

		// generate point-mutation of the (probabilistically) top-ranked
		pos = 1;
		while (toMutate > 0) {
			do {
				int mutated = rnd.nextInt(pos);
				try {
					instancePool[toFill] = (Instance) instancePool[base
							+ mutated].clone();
				} catch (CloneNotSupportedException e) {
					System.err.println("Shouldn't happen.");
				}
				instancePool[toFill].pointMutation();
			} while (instancePool[toFill].getLastMutationType() == Instance.NOMUTATION);
			if (pos < populationSize - base)
				pos++;
			toFill--;
			toMutate--;
		}

		// generate cross overs
		pos = 2;
		while (toFill >= 0) {
			int parent1 = base + rnd.nextInt(pos);
			int parent2 = base + rnd.nextInt(pos);
			while (parent1 == parent2)
				parent2 = base + rnd.nextInt(pos);
			Instance i1 = instancePool[parent1];
			Instance i2 = instancePool[parent2];
			double preference1 = i1.getFitness()
					/ (i1.getFitness() + i2.getFitness());
			instancePool[toFill] = i1.crossOver(i2, preference1);
			if (pos < populationSize - base)
				pos++;
			toFill--;
		}

		// evaluate them
		computeFitness(0, base);

		// sort
		Arrays.sort(instancePool);
		ObjectSeq.reverse(instancePool);
		bestFitness = instancePool[0].getFitness();
		bestInstance = instancePool[0];
		// System.err.println(Arrays.asList(instancePool));
		dumpPopulation();
	}

	public void dumpPopulation() {
		double lastFitness = Double.MIN_VALUE;
		boolean same = true;
		for (int i = 0; i < populationSize; i++) {
			if (instancePool[i].getFitness() == lastFitness) {
				same = true;
				System.err.print("=");
			} else {
				if (!same)
					System.err.print(" ");
				lastFitness = instancePool[i].getFitness();
				System.err.print(Math.round(lastFitness * 1000) / 1000.0);
				same = false;
			}
		}
		System.err.println();
	}

	/**
	 * Return the top ranked instances
	 */
	public Instance[] getTopRank(int rank) {
		if (rank > instancePool.length)
			rank = instancePool.length;

		Instance[] result = new Instance[rank];
		System.arraycopy(instancePool, 0, result, 0, rank);
		return result;
	}

	/**
	 * Compute the fitness of a given segment.
	 */
	protected void computeFitness(int start, int end) {
		for (int i = start; i < end; i++) {
			if (debug) {
				if (i % 100 == 0)
					System.err.print(" [" + i + "]");
			}
			computeFitness(instancePool[i]);
		}
		if (debug)
			System.err.println();

		waitForComputing();
	}

	/**
	 * Keep the current thread waiting until all pending computing tasks are
	 * finished.
	 */
	synchronized protected void waitForComputing() {
		while (computing)
			try {
				this.wait();
				// Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
			}
	}

	/**
	 * Compute the fitness of a given instance.
	 */
	protected void computeFitness(Instance instance) {
		Object id = computer.createID();
		computingTasks.put(id, instance);
		setComputing(true);
		computer.scheduleCompute(id, instance);
	}

	synchronized protected void setComputing(boolean computing) {
		this.computing = computing;
	}

	synchronized public void computationPerformed(ComputationEvent e) {
		Instance instance = computingTasks.remove(e.getSource());
		if (instance == null)
			System.err.println("Not found: " + e.getSource());
		else
			instance.setFitness(((Double) e.getPayload()).doubleValue());
		if (computingTasks.isEmpty()) {
			computing = false;
			this.notify();
		}
	}
}
