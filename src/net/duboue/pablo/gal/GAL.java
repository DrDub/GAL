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

package net.duboue.pablo.gal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import net.duboue.pablo.search.FitnessComputer;
import net.duboue.pablo.search.GeneticSearch;
import net.duboue.pablo.search.Instance;
import net.duboue.pablo.search.InstanceFactory;
import net.duboue.pablo.util.ObjectSeq;

public class GAL extends GeneticSearch {

	protected transient Instance[] newInstancePool;
	protected transient Instance[] neighborhood;
	protected int side;

	private double crossOverProbability;

	public GAL(FitnessComputer computer, Properties properties) {
		super(computer, Integer.parseInt(properties.getProperty(
				"net.duboue.pablo.gal.side", "200"))
				* Integer.parseInt(properties.getProperty(
						"net.duboue.pablo.gal.side", "200")), 0.0 /* not used */,
				0.0 /* not used */);
		this.crossOverProbability = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.crossOverProbability", "0.7"));
		this.side = Integer.parseInt(properties.getProperty(
				"net.duboue.pablo.gal.side", "200"));
	}

	public void startPopulation(InstanceFactory factory, int initialMutation) {
		this.startPopulation(factory);
	}

	public void startPopulation(InstanceFactory factory) {
		// create the initial pool by generating them at random
		this.instancePool = new Instance[populationSize];
		this.newInstancePool = new Instance[populationSize];
		this.neighborhood = new Instance[9];
		for (int i = 0; i < instancePool.length; i++)
			instancePool[i] = factory.newInstance();

		// compute the fitness of all of them
		computeFitness(0, populationSize);

		// best fitness
		bestInstance = instancePool[1];
		bestFitness = bestInstance.getFitness();
		for (int i = 1; i < instancePool.length; i++)
			if (instancePool[i].getFitness() > bestFitness) {
				bestFitness = instancePool[i].getFitness();
				bestInstance = instancePool[i];
			}
		// dumpPopulation();
	}

	public void setInitialPopulation(Set<Instance> population) {
		int c = 0;
		Iterator<Instance> p = population.iterator();
		this.instancePool = new Instance[populationSize];
		while (p.hasNext()) {
			instancePool[c] = p.next();
			c++;
		}
		// best fitness
		bestInstance = instancePool[1];
		bestFitness = bestInstance.getFitness();
		for (int i = 1; i < instancePool.length; i++)
			if (instancePool[i].getFitness() > bestFitness) {
				bestFitness = instancePool[i].getFitness();
				bestInstance = instancePool[i];
			}

		// System.err.println(Arrays.asList(instancePool));
	}

	/**
	 * Perform a step of genetic search.
	 */
	@Override
	public void step() {
		computeFitness(0, instancePool.length);

		for (int i = 0; i < instancePool.length; i++) {
			Instance parent1 = instancePool[i];

			getNeighborhood(i, neighborhood);

			Instance parent2 = select(neighborhood);
			Instance child = null;
			if (rnd.nextDouble() > crossOverProbability)
				try {
					child = (Instance) parent1.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.toString());
				}
			else
				child = parent1.crossOver(parent2, 0);
			child.pointMutation();
			newInstancePool[i] = child;
		}
		Instance[] temp = instancePool;
		instancePool = newInstancePool;
		computeFitness(0, instancePool.length);

		// compare and set
		instancePool = temp;
		for (int i = 0; i < instancePool.length; i++) {
			if (instancePool[i].getFitness() <= newInstancePool[i].getFitness())
				instancePool[i] = newInstancePool[i]; // replacement
			newInstancePool[i] = null;
		}
		System.gc();

		// best fitness
		bestInstance = instancePool[1];
		bestFitness = bestInstance.getFitness();
		for (int i = 1; i < instancePool.length; i++)
			if (instancePool[i].getFitness() > bestFitness) {
				bestFitness = instancePool[i].getFitness();
				bestInstance = instancePool[i];
			}
		// dumpPopulation();
	}

	protected void getNeighborhood(int i, Instance[] neighborhood) {
		int c = 0;
		int x = i / side;
		int y = i % side;
		neighborhood[c] = instancePool[i];
		c++;
		neighborhood[c] = instancePool[x * side + (y + 1) % side];
		c++;
		neighborhood[c] = instancePool[x * side + (side + y - 1) % side];
		c++;
		neighborhood[c] = instancePool[((x + 1) % side) * side + y];
		c++;
		neighborhood[c] = instancePool[((x + 1) % side) * side + (y + 1) % side];
		c++;
		neighborhood[c] = instancePool[((x + 1) % side) * side + (side + y - 1)
				% side];
		c++;
		neighborhood[c] = instancePool[((side + x - 1) % side) * side + y];
		c++;
		neighborhood[c] = instancePool[((side + x - 1) % side) * side + (y + 1)
				% side];
		c++;
		neighborhood[c] = instancePool[((side + x - 1) % side) * side
				+ (side + y - 1) % side];
		c++;
	}

	protected Instance select(Instance[] neighborhood) {
		Instance result = neighborhood[0];
		double bestFitness = result.getFitness();
		for (int i = 1; i < neighborhood.length; i++)
			if (neighborhood[i].getFitness() >= bestFitness) {
				result = neighborhood[i];
				bestFitness = neighborhood[i].getFitness();
			}
		return result;
	}

	public void dumpPopulation() {
		for (int i = 0; i < side; i++) {
			for (int j = 0; j < side; j++)
				System.err.print(Math.round(instancePool[i * side + j]
						.getFitness() * 1000)
						/ 1000.0 + " ");
			System.err.println();
		}
		System.err.println();
	}

	/**
	 * Return the top ranked instances
	 */
	public Instance[] getTopRank(int rank) {
		if (rank > instancePool.length)
			rank = instancePool.length;

		// sort
		Instance[] copy = new Instance[instancePool.length];
		System.arraycopy(instancePool, 0, copy, 0, copy.length);

		Arrays.sort(copy);
		ObjectSeq.reverse(copy);

		Instance[] result = new Instance[rank];
		System.arraycopy(copy, 0, result, 0, rank);
		return result;
	}
}
