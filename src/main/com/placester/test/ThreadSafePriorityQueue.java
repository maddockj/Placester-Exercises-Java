package com.placester.test;

import java.util.Arrays;
import java.util.Comparator;

// NOTE: we are aware that there is a PriorityQueue in
// java.util. Please do not use this. 
// If you are doing this test at home, please do not use any containers from
// java.util in your solution, as this is a test of data
// structure knowledge, rather than a test of java library knowledge.
// If you are doing it in the office, please ask the person testing you if you are going to
// use any built in collections other than arrays.

/*
 * The task is as follows: implement this class as you see fit, and get the unit test in
 * src/test/com/placester/test/PriorityQueueTest to pass. This class
 * must allow dynamic resizing as elements are added. What the
 * strategy is to do this is entirely up to you modulo the previously
 * stated constraints.
 * 
 * Feel free to use anything from Java.util.Arrays (e.g., you don't need to implement
 * your own sort if you don't want to).
 */
public class ThreadSafePriorityQueue<X> implements SimpleQueue<Priority<X>>
{
	/**
	 * The default initial capacity for the queue
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The array holding our queue of Priorities
	 */
	private Object[] q;
	
	/**
	 * The number of Priorities in the queue (not the capacity of the underlying array)
	 */
	private int size;
	
	/**
	 * A flag that indicates if a Priority has been added to the queue since it was last sorted
	 */
	private boolean isDirty;

	/**
	 * Create a queue with the default capacity
	 */
	public ThreadSafePriorityQueue()
	{
		initialize();
	}

	/**
	 * Set initial values for this queue
	 */
	public void initialize()
	{
		q = new Object[DEFAULT_INITIAL_CAPACITY];
		size = 0;
		isDirty = false;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return size == 0;
	}

	@Override
	public synchronized void clear()
	{
		// Assumption: no need to reset the capacity when emptying the current queue
		q = new Object[q.length];
		size = 0;
	}

	@Override
	/**
	 * Synchronized because we're adding to the queue and changing the dirty flag.
	 * Just adds the new element to the next slot in the queue array; leaves priority sorting
	 * until a read operation is necessary so that we're not generating a fresh array with 
	 * every insert.
	 */
	public synchronized boolean add(Priority<X> e)
	{
		checkCapacity(size + 1);
		try
		{
			// Adds to the next open slot, then increments the size counter
			q[size++] = e;
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			throw new IllegalStateException("No space available in queue", ex);
		}
		isDirty = true;
		return true;
	}

	/**
	 * Make sure the queue capacity is at least the given minimum.
	 * If the current capacity is too small, double it (or use the given minimum if that
	 * is larger than double the current capacity).
	 * Synchronized for thread-safe manipulation of the size the underlying array.
	 * 
	 * @param minCapacity a minimum queue capacity to be enforced
	 */
	private synchronized void checkCapacity(int minCapacity)
	{
		if (q.length < minCapacity)
		{
			Object[] oldQ = q;
			int newCapacity = Math.max(minCapacity, q.length * 2);
			q = Arrays.copyOf(oldQ, newCapacity);
		}
	}

	@Override
	/**
	 * Synchronized because we're removing an element from the queue
	 */
	public synchronized Priority<X> poll()
	{
		// Get the head element
		Priority<X> p = peek();
		if (p != null)
		{
			// Empty the space it came from in the queue
			q[size - 1] = null;

			// Decrement the number of elements in the queue
			size--;
		}

		return p;
	}

	@Override
	public Priority<X> peek()
	{
		if (size == 0)
		{
			return null;
		}

		// Put the queue in priority order so we can grab the right element
		sortQueue();

		// The queue is sorted from high value (low priority) to low value (high priority),
		// so grab the one on the end with the highest priority
		@SuppressWarnings("unchecked")
		Priority<X> p = (Priority<X>) q[size - 1];

		return p;
	}

	@Override
	public boolean contains(Priority<X> x)
	{
		if (x == null)
		{
			// Per the super description, check for null
			for (int i=0; i<size-1; i++)
			{
				if (q[i] == null)
				{
					return true;
				}
			}
		}
		else
		{
			// Check for an equal Priority
			for (int i=0; i<size-1; i++)
			{
				if (x.equals(q[i]))
				{
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Sort the queue from high value (low priority) to low value (high priority).
	 * Only sorts if any elements have been added since the last sort.
	 * Synchronized for thread-safe manipulation of the order of elements in the queue.
	 */
	private synchronized void sortQueue()
	{
		if (!isDirty)
		{
			return;
		}

		Arrays.sort(q, new PriorityComparator());

		isDirty = false;
	}

	/**
	 * Compares priority objects and orders them by their priority integer value,
	 * from high to low. Null elements (indicating open slots in the queue)
	 * are shuffled to the right of the lowest value Priority.
	 */
	private class PriorityComparator implements Comparator<Object>
	{
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object o1, Object o2)
		{
			Priority<X> p1 = null;
			Priority<X> p2 = null;

			// Just some type safety checking
			if (o1 instanceof Priority)
			{
				p1 = (Priority<X>) o1;
			}

			if (o2 instanceof Priority)
			{
				p2 = (Priority<X>) o2;
			}

			// Move nulls to the right
			if (p1 == null)
			{
				if (p2 == null)
				{
					return 0;
				}

				return 1;
			}
			else if (p2 == null)
			{
				return -1;
			}

			// Sort descending by priority integer value
			if (p1.priority() < p2.priority())
			{
				return 1;
			}

			if (p1.priority() > p2.priority())
			{
				return -1;
			}

			return 0;
		}

	}
}
