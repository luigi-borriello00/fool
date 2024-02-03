package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

	static Map<String, String> superType = new HashMap<String, String>();

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		return a.getClass().equals(b.getClass()) ||
				((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				(a instanceof EmptyTypeNode) && (b instanceof RefTypeNode) ||
				isSubClass(a,b) || isOverride(a,b);
	}

	private static boolean isSubClass(TypeNode a, TypeNode b) {
		if(a instanceof RefTypeNode && b instanceof RefTypeNode) {
			String cl1 = ((RefTypeNode) a).id;
			String cl2 = ((RefTypeNode) b).id;
			while (superType.containsKey(cl1)) {
				if (superType.get(cl1).equals(cl2)) {
					return true;
				} else {
					cl1 = superType.get(cl1);
				}
			}
		}
		return false;
	}

	private static boolean isOverride(TypeNode a, TypeNode b) {
		if(a instanceof ArrowTypeNode && b instanceof ArrowTypeNode) {
			if(isSubtype(((ArrowTypeNode) a).ret, ((ArrowTypeNode) b).ret)){
				for(int i = 0; i < ((ArrowTypeNode) a).parlist.size(); i++){
					if(!isSubtype(((ArrowTypeNode) b).parlist.get(i), ((ArrowTypeNode) a).parlist.get(i))){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

}
