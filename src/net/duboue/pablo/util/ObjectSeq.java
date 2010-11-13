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

/**
 * A meaningful hash value for an array of objects and assorted sequence of
 * objects functions.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 * @version 0.02
 */

public class ObjectSeq {
	public ObjectSeq(Object[] seq) {
		this.seq = seq;
	}

	public Object[] seq;

	public int hashCode() {
		int base = 113;
		int hash = 0;
		for (int i = 0; i < seq.length; i++)
			hash = (hash + seq[i].hashCode()) * base;
		return hash;
	}

	public boolean equals(Object o) {
		Object[] oSeq;
		if (o instanceof ObjectSeq) {
			oSeq = ((ObjectSeq) o).seq;
		} else if (o instanceof Object[]) {
			oSeq = (Object[]) o;
		} else
			return false;
		if (oSeq.length != this.seq.length)
			return false;
		for (int i = 0; i < this.seq.length; i++)
			if (!this.seq[i].equals(oSeq[i]))
				return false;
		return true;
	}

	public String toString() {
		String result = "ObjectSeq[ ";
		for (int i = 0; i < seq.length; i++)
			result = result + seq[i].toString() + " ";
		return result + "]";
	}

	public Object[] toArray() {
		return seq;
	}

	/**
	 * Reverses an array (in-place).
	 */
	public static void reverse(Object[] a) {
		for (int i = 0; i < a.length / 2; i++) {
			Object t = a[i];
			a[i] = a[a.length - i - 1];
			a[a.length - i - 1] = t;
		}
	}
}
