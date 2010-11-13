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

import java.util.Properties;

import net.duboue.pablo.search.FitnessComputer;
import net.duboue.pablo.search.Instance;

public class GalFitnessComputer extends FitnessComputer {

	private int[][] sequences;

	private double weightConsistency;
	private double weightGeneralization;
	private double weightSize;

	private double m1;
	private double m2;
	private double max;
	private double fLimit;

	private double maxSize;

	private float limit;
	private int targetLanguageSize;

	public GalFitnessComputer(int[][] sequences, Properties properties) {
		super();
		this.sequences = sequences;

		this.weightConsistency = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.weightConsistency", "0.33"));
		this.weightSize = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.weightSize", "0.33"));
		this.weightGeneralization = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.weightGeneralization", "0.33"));
		this.targetLanguageSize = (int) (sequences.length * Double
				.parseDouble(properties.getProperty(
						"net.duboue.pablo.gal.targetSizeMultiplier", "2.0")));
		this.limit = Float.parseFloat(properties.getProperty(
				"net.duboue.pablo.gal.generalizationLimit", "0.5"));

		this.m1 = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.generalizationM1", "2"));
		this.m2 = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.generalizationM2", "0.025"));
		this.max = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.generalizationMax", "20"));

		this.maxSize = Double.parseDouble(properties.getProperty(
				"net.duboue.pablo.gal.sizeMax", "20"));

		this.fLimit = max / (1.0 + m1 * limit * limit);
	}

	public double debug(Instance instance) {
		return compute(instance, true);
	}

	protected double compute(Instance instance) {
		return compute(instance, false);
	}

	protected double compute(Instance instance, boolean debug) {
		FiniteStateAutomaton fsa = (FiniteStateAutomaton) instance;
		if (!debug && !Double.isNaN(instance.getFitness()))
			return instance.getFitness();

		fsa.removeCycles();

		double consistencyFitness = consistencyFitness(sequences, fsa, debug);
		double sizeFitness = sizeFitness(fsa, maxSize);
		double generalizationFitness = generalizationFitness(fsa,
				targetLanguageSize, limit);
		double fitness = consistencyFitness * weightConsistency + sizeFitness
				* weightSize + generalizationFitness * weightGeneralization;
		if (debug) {
			System.out.println("\tconsistencyFitness = " + consistencyFitness);
			System.out.println("\tsizeFitness = " + sizeFitness + " [ "
					+ fsa.size() + " / " + maxSize + " ]");
			System.out.println("\tgeneralizationFitness = "
					+ generalizationFitness + " [ " + fsa.languageSize()
					+ " / " + targetLanguageSize + " ]");
		}

		instance.setFitness(fitness);

		return fitness;
	}

	private double consistencyFitness(int[][] sequences,
			FiniteStateAutomaton fsa, boolean debug) {
		// recognize prefixes on the sequences
		double result = 0.0;

		int fully = 0;
		for (int i = 0; i < sequences.length; i++) {
			int[] seq = sequences[i];
			int l = seq.length;
			int ret = 0;
			do {
				ret = fsa.traverse(seq, l);
				l--;
			} while (l > 0 && ret < 0);
			if (ret >= 0) {
				result += (l + 1.0) / seq.length
						- (ret == 0 ? 1.0 / (2.0 * seq.length) : 0);
				if (ret > 0)
					fully++;
			}
		}
		if (debug)
			System.out.println("\t\tFully recognized: " + fully);

		return result / sequences.length;
	}

	private double sizeFitness(FiniteStateAutomaton fsa, double maxSize) {
		// number of states on automaton
		return Math.sqrt(1.0 - fsa.size() / maxSize);
	}

	private double generalizationFitness(FiniteStateAutomaton fsa,
			int targetLanguageSize, float limit) {
		int actual = fsa.languageSize();
		int diff = targetLanguageSize - actual;
		int absDiff = Math.abs(diff);
		double result = absDiff <= limit ? max / (1.0 + m1 * diff * diff)
				: -1.0 * m2 * (absDiff - limit) + fLimit;
		return result / max;
	}

}
