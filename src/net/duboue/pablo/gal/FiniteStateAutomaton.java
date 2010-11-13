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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import net.duboue.pablo.search.Instance;
import net.duboue.pablo.search.InstanceFactory;

public class FiniteStateAutomaton implements Instance, Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String[] alphabet;

	protected int numberOfNodes;
	protected int[][] transitionTable;
	protected Random random = new Random();

	public static int newInstanceMin = 6;
	public static int newInstanceMax = 12;
	public static int offsetMinus = -2;
	public static int offsetPlus = 2;
	public static int offsetVarMinus = -1;
	public static int offsetVarPlus = 3;
	public static boolean useTargetedMutations = false;
	public static double noTransitionProbability = 0.3;
	public static double lowerStateProbability = 0.5;

	public FiniteStateAutomaton(Random random, Reader reader)
			throws IOException {
		super();

		this.random = random;
		BufferedReader br = new BufferedReader(reader);
		String line = br.readLine();
		this.numberOfNodes = Integer.parseInt(line.split("=", 0)[1]);
		line = br.readLine();
		int numberOfSymbols = Integer.parseInt(line.split("=", 0)[1]);
		line = br.readLine();
		this.fitness = Double.parseDouble(line.split("=", 0)[1]);
		this.alphabet = new String[numberOfSymbols];
		for (int i = 0; i < numberOfSymbols; i++) {
			line = br.readLine();
			this.alphabet[i] = line.split("=", 0)[1];
		}
		this.transitionTable = new int[numberOfNodes][];
		for (int i = 0; i < numberOfNodes; i++) {
			this.transitionTable[i] = new int[alphabet.length];
			for (int j = 0; j < alphabet.length; j++) {
				line = br.readLine();
				this.transitionTable[i][j] = Integer.parseInt(line
						.split("=", 0)[1]);
			}
		}
		br.close();
	}

	public FiniteStateAutomaton(FiniteStateAutomaton other) {
		super();
		this.alphabet = other.alphabet;
		this.random = other.random;
		this.fitness = other.fitness;
		this.numberOfNodes = other.numberOfNodes;
		this.transitionTable = new int[numberOfNodes][];
		for (int i = 0; i < numberOfNodes; i++) {
			this.transitionTable[i] = new int[alphabet.length];
			for (int j = 0; j < alphabet.length; j++)
				this.transitionTable[i][j] = other.transitionTable[i][j];
		}
	}

	public FiniteStateAutomaton(String[] alphabet, Random random,
			int[] chromosome) {
		super();
		this.random = random;
		this.alphabet = alphabet;
		this.fitness = Double.NaN;
		setChromosome(chromosome);
	}

	protected void setChromosome(int[] chromosome) {
		int c = 0;
		// numbers
		this.numberOfNodes = chromosome[c];
		c++;
		// transition table
		this.transitionTable = new int[numberOfNodes][];
		for (int i = 0; i < numberOfNodes; i++) {
			transitionTable[i] = new int[alphabet.length];
			for (int j = 0; j < alphabet.length; j++) {
				transitionTable[i][j] = chromosome[c];
				c++;
			}
		}
		if (c != chromosome.length)
			// throw new
			// IllegalStateException("chromosome.length=="+chromosome.length+" c=="+c);
			System.err.println("** warning, chromosome.length=="
					+ chromosome.length + " c==" + c);
	}

	public int chromosomeLength(int numberOfNodes) {
		return chromosomeLength(alphabet, numberOfNodes);
	}

	public static int chromosomeLength(String[] alphabet, int numberOfNodes) {
		return 1 + // number of nodes
				numberOfNodes * alphabet.length // transition table
		;
	}

	public int[] toChromosome() {
		int size = chromosomeLength(numberOfNodes);
		int[] result = new int[size];
		int c = 0;
		result[c] = this.numberOfNodes;
		c++;
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < alphabet.length; j++) {
				result[c] = transitionTable[i][j];
				c++;
			}
		return result;
	}

	public int size() {
		return numberOfNodes;
	}

	public int[][] getTransitionTable() {
		return transitionTable;
	}

	public void print(PrintWriter pw) {
		pw.println("numberOfNodes=" + numberOfNodes);
		pw.println("numberOfSymbols=" + alphabet.length);
		pw.println("fitness=" + fitness);
		for (int i = 0; i < alphabet.length; i++)
			pw.println("symbol_" + i + "=" + alphabet[i]);
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < alphabet.length; j++) {
				pw.print("transitionTable[" + i + "][" + j + "/ '"
						+ alphabet[j] + "']=");
				if (transitionTable[i][j] == -1)
					pw.println();
				else
					pw.println(transitionTable[i][j]);
			}
	}

	public void printAsDot(PrintWriter pw) {
		pw.println("digraph fsa{");
		pw.println("  node [height=0.5,width=0.5]");
		pw.println("  rankdir=LR");
		for (int i = 0; i < numberOfNodes; i++)
			pw.println("  n" + i + " [label=\"" + i + "\""
					+ (i == numberOfNodes - 1 ? " style=bold" : "") + "]");

		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < alphabet.length; j++)
				if (transitionTable[i][j] != -1)
					pw.println("  n" + i + "->n" + transitionTable[i][j]
							+ " [label=\"" + alphabet[j] + "\"]");
		pw.println(" fitness [label=\"fitness " + fitness + "\" color=white]");
		pw.println("}");
	}

	public Object clone() throws CloneNotSupportedException {
		return new FiniteStateAutomaton(this);
	}

	// ////// Randomizable
	public void setRandom(Random random) {
		this.random = random;
	}

	public Random getRandom() {
		return random;
	}

	// ////// Instance

	protected double fitness;

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int lastMutationType;

	/** Last performed mutation */
	;

	public int getLastMutationType() {
		return lastMutationType;
	}

	/**
	 * Perform a point mutation in the current instance. Mutate(child) procedure
	 * in GAL.
	 */
	public void pointMutation() {
		lastMutationType = SHUFFLE;

		int[] chromosome = this.toChromosome();
		if (useTargetedMutations) {
			int i = random.nextInt(numberOfNodes * alphabet.length);
			_mutate(alphabet, random, chromosome, i, this.numberOfNodes);
		} else {
			for (int i = 0; i < numberOfNodes * alphabet.length; i++) {
				if (random.nextDouble() < 1.0 / chromosome.length)
					_mutate(alphabet, random, chromosome, i, this.numberOfNodes);
			}
		}
	}

	protected static void _mutate(String[] alphabet, Random random,
			int[] chromosome, int i, int numberOfN) {
		int state = i / alphabet.length;
		if (random.nextDouble() < noTransitionProbability)
			chromosome[1 + i] = -1;
		else if (random.nextDouble() < lowerStateProbability)
			chromosome[1 + i] = state == 0 ? 0 : random.nextInt(state);
		else
			chromosome[1 + i] = numberOfN - 1 - state == 0 ? state
					: (state + random.nextInt(numberOfN - 1 - state));
	}

	/**
	 * Point mutation: instance growth.
	 */
	public void pointMutationGrow() {
		this.pointMutation();
	}

	/**
	 * Point mutation: instance shrinking.
	 */
	public void pointMutationShrink() {
		this.pointMutation();
	}

	/**
	 * Point mutation: instance shuffling.
	 */
	public void pointMutationShuffle() {
		this.pointMutation();
	}

	/**
	 * Cross-over with another instance (generates a third instance). The
	 * parents are kept unmodified. It implements FPU_Crossover from GAL.
	 * 
	 * @param other
	 *            the other Instance to cross over with.
	 * @param preferThis
	 *            the degree of preference for the current instance (ignored).
	 * 
	 * @return a newly created instance.
	 */
	public Instance crossOver(Instance other, double preferThis) {
		int newNumberOfNodes = (this.numberOfNodes + offsetMinus + random
				.nextInt(offsetPlus - offsetMinus));
		newNumberOfNodes = newNumberOfNodes < 1 ? 1 : newNumberOfNodes;
		int[] chromosome = new int[chromosomeLength(newNumberOfNodes)];
		chromosome[0] = newNumberOfNodes;
		int[] parent1 = this.toChromosome();
		int[] parent2 = ((FiniteStateAutomaton) other).toChromosome();
		int childTTlength = newNumberOfNodes * alphabet.length;
		int parent1TTlength = this.numberOfNodes * alphabet.length;
		int parent2TTlength = ((FiniteStateAutomaton) other).numberOfNodes
				* alphabet.length;

		// transitions
		int i = 1;
		while (i < 1 + childTTlength && i < 1 + parent1TTlength
				&& i < 1 + parent2TTlength) {
			chromosome[i] = (random.nextDouble()
					* (this.getFitness() + other.getFitness()) > this
					.getFitness() ? parent2[i] : parent1[i])
					% newNumberOfNodes;
			i++;
		}
		if (i < 1 + childTTlength) {
			while (i < 1 + childTTlength && i < 1 + parent1TTlength) {
				chromosome[i] = (parent1[i] % newNumberOfNodes);
				i++;
			}
			while (i < 1 + childTTlength && i < 1 + parent2TTlength) {
				chromosome[i] = (parent2[i] % newNumberOfNodes);
				i++;
			}
			while (i < 1 + childTTlength) {
				_mutate(alphabet, random, chromosome, i - 1, newNumberOfNodes);
				i++;
			}
		}

		return new FiniteStateAutomaton(alphabet, random, chromosome);
	}

	/**
	 * @return true if the automata matches the sequence, irrespective of where
	 *         it ends.
	 */
	public boolean matches(int[] seq) {
		return traverse(seq) >= 0;
	}

	/**
	 * @return true if the automata matches the sequence and ending in a final
	 *         state.
	 */
	public boolean accepts(int[] seq) {
		return traverse(seq) > 0;
	}

	/**
	 * @return -1 if it doesn't match, 0 if it matches but it doesn't accept and
	 *         1 otherwise.
	 */
	public int traverse(int[] seq) {
		return traverse(0, seq, 0, seq.length);
	}

	/**
	 * same as traverse but up till a given length.
	 */
	public int traverse(int[] seq, int length) {
		return traverse(0, seq, 0, length);
	}

	protected int traverse(int start, int[] seq, int offset, int length) {
		if (offset == length)
			if (start == transitionTable.length - 1)
				return 1;
			else
				return 0;
		if (this.transitionTable[start][seq[offset]] == -1)
			return -1;
		return traverse(this.transitionTable[start][seq[offset]], seq,
				offset + 1, length);
	}

	/**
	 * Removes an edge at the end of a cycle. Enforces accepting a finite
	 * language.
	 */
	public void removeCycles() {
		removeCycles(0, new HashSet<Integer>());
	}

	private void removeCycles(int current, Set<Integer> visited) {
		visited.add(current);
		for (int i = 0; i < alphabet.length; i++) {
			if (this.transitionTable[current][i] == -1)
				continue;
			if (visited.contains(this.transitionTable[current][i])) {
				// hey, that's a cycle!
				this.transitionTable[current][i] = -1;
			} else {
				// recurse
				removeCycles(this.transitionTable[current][i],
						new HashSet<Integer>(visited));
			}
		}
	}

	public int languageSize() {
		return languageSize(0, new HashSet<Integer>());
	}

	private int languageSize(int current, HashSet<Integer> visited) {
		if (current == this.transitionTable.length - 1)
			return 1; // we reached accepting state

		visited.add(current);
		int result = 0;
		for (int i = 0; i < alphabet.length; i++) {
			if (this.transitionTable[current][i] == -1)
				continue;
			if (visited.contains(this.transitionTable[current][i])) {
				// hey, that's a cycle!
				return Integer.MAX_VALUE; // cycles == infinite
			} else {
				// recurse
				int recurse = languageSize(this.transitionTable[current][i],
						new HashSet<Integer>(visited));
				if (recurse == Integer.MAX_VALUE)
					return recurse;
				result += recurse;
			}
		}

		return result;
	}

	/**
	 * Compare two instances on the basis of their fitness.
	 */
	public int compareTo(Instance other) {
		return (other.getFitness() == this.fitness ? 0 : (this.fitness < other
				.getFitness() ? -1 : 1));
	}

	public static class Factory implements InstanceFactory {
		private String[] alphabet;

		private Random random;

		public Factory(String[] alphabet, Random random, Properties properties) {
			this.alphabet = alphabet;
			this.random = random;
		}

		public Instance newInstance() {
			int newNumberOfNodes = (newInstanceMin + random
					.nextInt(newInstanceMax - newInstanceMin));
			int[] chromosome = new int[chromosomeLength(alphabet,
					newNumberOfNodes)];
			chromosome[0] = newNumberOfNodes;
			for (int i = 0; i < newNumberOfNodes * alphabet.length; i++) {
				_mutate(alphabet, random, chromosome, i, newNumberOfNodes);
			}
			return new FiniteStateAutomaton(alphabet, random, chromosome);
		}
	}
}
