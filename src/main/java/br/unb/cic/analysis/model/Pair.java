package br.unb.cic.analysis.model;

import java.util.Objects;

public class Pair<T, U> {
	private T first; 
	private U second; 
	
	public Pair(T first, U second) {
		this.first = first;
		this.second = second; 
	}
	
	public T getFirst() {
		return first;
	}
	
	public U getSecond() {
		return second; 
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) &&
				Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
