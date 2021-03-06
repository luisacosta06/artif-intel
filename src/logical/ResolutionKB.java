package logical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ResolutionKB {
	
	Set<Sentence> sentences = new HashSet<Sentence>();
	
	public void tell(Sentence sentence) {
		sentences.add(sentence.toCnf());
	}
	
	/**
	 * PL_RESOLUTION()
	 * AIMA3 Figure 7.12 page 255.
	 */
	public boolean query(Sentence alpha) {
				
		List<Or> clauses = makeContradiction(alpha);
		
		Set<Or> newClauses = new HashSet<Or>();
		while(true) {
			
			for(int i = 0; i < clauses.size(); i++) {

				for(int j = i + 1; j < clauses.size(); j++) {
					
					Set<Or> resolvents = resolve(clauses.get(i), clauses.get(j));
					if (containsEmptyClause(resolvents)) {
						return true;
					}
					newClauses.addAll(resolvents);					
				}
			}
			if (clauses.containsAll(newClauses)) {
				return false;
			}
			clauses.addAll(newClauses);			
		}
	}

	private List<Or> makeContradiction(Sentence alpha) {
		List<Sentence> kb = new LinkedList<Sentence>(sentences);
		kb.add(new Not(alpha));		
		And and = (And)(new And(kb).toCnf());
		
		List<Or> result = new ArrayList<Or>();
		for(Sentence s : and.conjuncts()) {
			if (s instanceof Or )
				result.add((Or)s);
			else
				result.add(new Or(s));
		}
		return result;		
	}
	
	private boolean containsEmptyClause(Set<Or> resolvents) {
		for(Or or : resolvents) {			
			if (or.disjuncts().isEmpty())
				return true;
		}
		return false;
	}
	
	private static Set<Symbol> intersection(Set<Symbol> set1, Set<Symbol> set2) {
		Set<Symbol> result = new HashSet<Symbol>();
		for(Symbol sym : set1) {
			if (set2.contains(sym)) {
				result.add(sym);
			}				
		}
		return result;
	}

	public static Set<Or> resolve(final Or c1, final Or c2) {
		
		final Set<Symbol> symbols = intersection(c1.symbols(), c2.symbols());
		
		final Set<Or> result = new HashSet<Or>();
		new Combinator<Symbol>() {

			@Override
			public void foreach(Set<Symbol> set) {
				if (!set.isEmpty()) {
					result.add(resolve(set, c1, c2));
				}
			}
			
		}.generate(symbols);
		
		return result;
	}
	
	private static Or resolve(Set<Symbol> symbols, Or c1, Or c2) {

		Set<Sentence> filtered = new HashSet<Sentence>();
		
		for(Sentence s: c1.disjuncts()) {
			if (symbols.contains(symbol(s))) {				
				if (!complementsAny(s, c2))				
					filtered.add(s);
			}	
			else {
				filtered.add(s);
			}
		}
		
		for(Sentence s: c2.disjuncts()) {
			if (symbols.contains(symbol(s))) {				
				if (!complementsAny(s, c1))				
					filtered.add(s);
			}	
			else {
				filtered.add(s);
			}
		}
		
		return new Or(filtered);		
	}
	
	private static boolean complementsAny(Sentence literal, Or c2) {
		for(Sentence s : c2.disjuncts()) {
			if (complementary(literal, s))
				return true;
		}
		return false;
	}

	private static boolean complementary(Sentence lit1, Sentence lit2) {
		Symbol sym1 = symbol(lit1);
		Symbol sym2 = symbol(lit2);
		
		if (!sym1.equals(sym2))
			return false;
		
		boolean neg1 = lit1 instanceof Not;
		boolean neg2 = lit2 instanceof Not;
		
		return neg1 != neg2;
	}

	private static Symbol symbol(Sentence s) {
		if (s instanceof Not) {
			return (Symbol)((Not)s).sentence();
		}
		return (Symbol)s;
	}

}
