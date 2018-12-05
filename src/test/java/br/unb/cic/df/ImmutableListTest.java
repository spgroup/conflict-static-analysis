package br.unb.cic.df;

import static org.junit.Assert.*;
import org.junit.Test;

import br.unb.cic.df.analysis.ImmutableFlowSet;

public class ImmutableListTest {

	@Test
	public void testEmptyAfterNew() {
		ImmutableFlowSet<String> list = new ImmutableFlowSet<>();
		assertEquals(0, list.size());
	}
	
	@Test
	public void testAddElements() { 
		ImmutableFlowSet<Integer> set = new ImmutableFlowSet<>();
		
		set = set.add(123); 
		
		assertEquals(1, set.size());
		assertTrue(set.contains(123));
		
		set.add(456);
		
		assertEquals(1, set.size());
		assertTrue(set.contains(123));
		assertFalse(set.contains(456));	
		
		set = set.add(456); 
		
		assertEquals(2, set.size());
		assertTrue(set.contains(123));
		assertTrue(set.contains(456));	
	
	}
	
	@Test
	public void testIdempotentProperty() {
		ImmutableFlowSet<Integer> set = new ImmutableFlowSet<>();
		
		set = set.add(123); 
		
		assertEquals(1, set.size());
		assertTrue(set.contains(123));
		
		set = set.add(123);
		
		assertEquals(1, set.size());
		assertTrue(set.contains(123));
	}
	
	@Test
	public void testDiference() {
		ImmutableFlowSet<Integer> set1 = new ImmutableFlowSet<>();
		ImmutableFlowSet<Integer> set2 = new ImmutableFlowSet<>();
		
		set1 = set1.add(123); 
		set1 = set1.add(456);
		
		assertEquals(2, set1.size());
		assertTrue(set1.contains(123));
		assertTrue(set1.contains(456));
		
		set2 = set2.add(456); 
		
		set1 = set1.difference(set2);
		
		assertEquals(1, set1.size());
		assertTrue(set1.contains(123));
		assertFalse(set1.contains(456));
		
		assertEquals(1, set2.size());
		assertTrue(set2.contains(456));
	}

}
