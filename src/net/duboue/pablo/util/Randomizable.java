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

package net.duboue.pablo.util;

import java.util.Random;

/**
 * A randomizable interface, for objects that have some random generator, so it
 * can be operated from outside.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public interface Randomizable {
	public Random getRandom();

	public void setRandom(Random random);
}
