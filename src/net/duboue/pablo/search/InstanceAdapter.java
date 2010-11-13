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

import java.util.Random;

/**
 * An abstract class for instances in a genetic search.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public abstract class InstanceAdapter implements Instance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Random rnd = new Random();

	public Random getRandom() {
		return rnd;
	}

	public void setRandom(Random rnd) {
		InstanceAdapter.rnd = rnd;
	}

	protected double fitness;

	public InstanceAdapter() {
		this.fitness = Double.MIN_VALUE;
	}

	/*
	 * public static InstanceFactory getFactory(){ return new InstanceFactory(){
	 * public Instance newInstance(){return new InstanceAdapter(); } }; }
	 */
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	protected int lastMutation = 0;
	/** Last performed mutation */
	;

	public int getLastMutationType() {
		return lastMutation;
	}

	/**
	 * Perform a point mutation in the current instance.
	 */
	public void pointMutation() {
		double r = rnd.nextDouble();
		if (r < chooseGrowProb)
			pointMutationGrow();
		else if (r < chooseShrinkProb)
			pointMutationShrink();
		else
			pointMutationShuffle();
	}

	public void pointMutationGrow() {
		lastMutation = GROW;
	}

	public void pointMutationShrink() {
		lastMutation = SHRINK;
	}

	public void pointMutationShuffle() {
		lastMutation = SHUFFLE;
	}

	public abstract Instance crossOver(Instance other, double preferThis);

	public int compareTo(Instance other) {
		return (other.getFitness() == this.fitness ? 0 : (this.fitness < other
				.getFitness() ? -1 : 1));
	}

	public abstract Object clone() throws CloneNotSupportedException;
}
