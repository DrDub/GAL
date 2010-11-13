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

import net.duboue.pablo.util.Randomizable;

/**
 * An instance in a genetic search.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public interface Instance extends java.io.Serializable, Cloneable,
		Comparable<Instance>, Randomizable {
	public double getFitness();

	public void setFitness(double fitness);

	/** Point mutations: probability of choosing instance growth. */
	public static double chooseGrowProb = 0.2;

	/** Point mutations: probability of choosing instance shrinking. */
	public static double chooseShrinkProb = chooseGrowProb + 0.2;

	public static int NOMUTATION = 0;
	public static int GROW = 1;
	public static int SHRINK = 2;
	public static int SHUFFLE = 3;

	/** Last performed mutation */
	;

	public int getLastMutationType();

	/**
	 * Perform a point mutation in the current instance.
	 */
	public void pointMutation();

	/**
	 * Point mutation: instance growth.
	 */
	public void pointMutationGrow();

	/**
	 * Point mutation: instance shrinking.
	 */
	public void pointMutationShrink();

	/**
	 * Point mutation: instance shuffling.
	 */
	public void pointMutationShuffle();

	/**
	 * Cross-over with another instance (generates a third instance). The
	 * parents are kept unmodified.
	 * 
	 * @param other
	 *            the other Instance to cross over with.
	 * @param preferThis
	 *            the degree of preference for the current instance.
	 * 
	 * @return a newly created instance.
	 */
	public Instance crossOver(Instance other, double preferThis);

	/**
	 * Deep cloning operation.
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * Compare two instances on the basis of their fitness.
	 */
	public int compareTo(Instance o);
}
