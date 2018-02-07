package com.webperformance.muse.measurements

/**
 * Holds a group of measurements and provides an iterator to access them.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
interface Measurements
{
	fun iterator() : Iterator<Measurement>
}