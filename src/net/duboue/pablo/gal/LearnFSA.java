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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import net.duboue.pablo.search.FitnessComputer;
import net.duboue.pablo.search.InstanceFactory;

public class LearnFSA {
	public static void main(String[] args) throws IOException {
		System.out
				.println("LearnFSA version 0.1, Copyright (C) 2010 Pablo Ariel Duboue\n"
						+ "LearnFSA comes with ABSOLUTELY NO WARRANTY; see COPYING for details.\n"
						+ "This is free software, and you are welcome to redistribute it\n"
						+ "under certain conditions; see COPYING for details.\n");

		if (args.length < 2) {
			System.err
					.println("Must specify the training sequences file and a properties file.\n"
							+ "The training file has one sequence per line, the alphabet "
							+ "will be induced by tokens separated by whitespaces.\n");
			System.exit(-1);
		}
		String trainingFile = args[0];
		String propFile = args[1];
		boolean dumpBestPerGeneration = false;

		// properties
		InputStream is = ClassLoader.getSystemResourceAsStream(propFile);
		if (is == null)
			is = new FileInputStream(new File(propFile));
		Properties properties = new Properties();
		properties.load(is);
		is.close();

		dumpBestPerGeneration = Boolean.parseBoolean(properties.getProperty(
				"net.duboue.pablo.gal.dumpBestPerGeneration", "false"));

		// read sequences
		List<String[]> rawSequences = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				trainingFile)));
		String line = br.readLine();
		while (line != null) {
			rawSequences.add(line.split("\\s+"));
			line = br.readLine();
		}

		// transform sequences into numbers
		Set<String> alphabetSet = new HashSet<String>();
		for (String[] seq : rawSequences)
			for (String w : seq)
				alphabetSet.add(w);
		Map<String, Integer> alphabetMap = new HashMap<String, Integer>();
		String[] alphabet = new String[alphabetSet.size()];
		{
			int c = 0;
			for (String w : alphabetSet) {
				alphabet[c] = w;
				alphabetMap.put(w, c);
				c++;
			}
		}
		int[][] sequences = new int[rawSequences.size()][];
		for (int i = 0; i < rawSequences.size(); i++) {
			String[] seq = rawSequences.get(i);
			sequences[i] = new int[seq.length];
			for (int j = 0; j < seq.length; j++)
				sequences[i][j] = alphabetMap.get(seq[j]);
		}

		Random random = new Random(Integer.parseInt(properties.getProperty(
				"net.duboue.pablo.gal.seed", "1234")));

		InstanceFactory factory = new FiniteStateAutomaton.Factory(alphabet,
				random, properties);
		FitnessComputer computer = new GalFitnessComputer(sequences, properties);
		Thread[] computerThreads = new Thread[Runtime.getRuntime()
				.availableProcessors()];
		for (int i = 0; i < computerThreads.length; i++) {
			Thread computerThread = new Thread(computer);
			computerThread.start();
			computerThreads[i] = computerThread;
		}

		GAL gal = new GAL(computer, properties);

		gal.setRandom(random);
		System.out.print("Starting population...");
		gal.startPopulation(factory);
		System.out.print(" Done.");

		int totalIter = Integer.parseInt(properties.getProperty(
				"net.duboue.pablo.gal.generations", "10000"));
		for (int i = 0; i < totalIter; i++) {
			System.out.println("Iteration: " + i + " best fitness: "
					+ gal.getBestFitness());
			try {
				PrintWriter pw;
				if (dumpBestPerGeneration) {
					pw = new PrintWriter(new FileWriter(i + "-best.instance"));
					((FiniteStateAutomaton) gal.getBestInstance()).print(pw);
					pw.close();
				}
				pw = new PrintWriter(new FileWriter("best-instance.dot"));
				((FiniteStateAutomaton) gal.getBestInstance()).printAsDot(pw);
				pw.close();
				((GalFitnessComputer) computer).debug(gal.getBestInstance());

			} catch (IOException e) {
				System.err.println(e);
			}
			gal.step();
			// System.out.println();
			// gal.dumpPopulation();
		}
		FitnessComputer.stopComputers();
		for (Thread computerThread : computerThreads)
			computerThread.interrupt();
	}
}
