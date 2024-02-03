package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.*;

/**
 * This class contains the relations between types.
 * It is used to check if a type is a subtype of another type.
 */
public class TypeRels {

    /**
     * Map of the super types of each type.
     * It is filled in the {@link TypeCheckEASTVisitor}.
     * The key is the type, the value is the super type.
     */
    public static Map<String, String> superType = new HashMap<>();

    /**
     * Check if the first type is EmptyTypeNode and the second type is RefTypeNode.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is EmptyTypeNode and the second type is RefTypeNode, false otherwise
     */
    private static boolean isEmptyTypeAndRefType(final TypeNode first, final TypeNode second) {
        return ((first instanceof EmptyTypeNode) && (second instanceof RefTypeNode));
    }

    /**
     * Check if the first type is BoolTypeNode and the second type is IntTypeNode or BoolTypeNode.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is BoolTypeNode and the second type is IntTypeNode or BoolTypeNode, false otherwise
     */
    private static boolean isBoolAndInt(final TypeNode first, final TypeNode second) {
        return ((first instanceof BoolTypeNode) && (second instanceof IntTypeNode | second instanceof BoolTypeNode))
                || ((first instanceof IntTypeNode) && (second instanceof IntTypeNode));
    }

    /**
     * Get the List of super types of a given type.
     * It starts from the given type and traverses the inheritance tree
     * until it reaches the top of the tree.
     *
     * @param type The type to start from
     * @return The List of super types
     */
    private static List<String> getSuperTypeList(final String type) {
        List<String> list = new ArrayList<>();
        String current = type;
        while (current != null) {
            list.add(current);
            current = superType.get(current);
        }
        return list;
    }

    /**
     * Compute the lowest common ancestor of two types.
     * It traverses the inheritance tree of the first type
     * and checks if the second type is a subtype of any of the
     * super types.
     * If it is, then the super type is the lowest common ancestor.
     * It's used to compute the type of the if-then-else expression in {@link TypeCheckEASTVisitor}.
     *
     * @param first  The first type
     * @param second The second type
     * @return The lowest common ancestor of the two types
     */
    public static TypeNode lowestCommonAncestor(final TypeNode first, final TypeNode second) {
        if (isSubtype(first, second)) return second;
        if (isSubtype(second, first)) return first;
        if (!(first instanceof RefTypeNode firstRefTypeNode)) return null;
        return getSuperTypeList(firstRefTypeNode.refClassId)
                .stream()
                .map(RefTypeNode::new)
                .filter(typeOfSuperA -> isSubtype(second, typeOfSuperA))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the first type is a subtype of the second type.
     * It checks if the first type is a BoolTypeNode and the second type is an IntTypeNode or a BoolTypeNode,
     * if the first type is an EmptyTypeNode and the second type is a RefTypeNode,
     * if the first type is a subclass of the second type, or if the first type is a method override of the second type.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a subtype of the second type, false otherwise
     */
    public static boolean isSubtype(final TypeNode first, final TypeNode second) {
        return isBoolAndInt(first, second)
                || isEmptyTypeAndRefType(first, second)
                || isSubclass(first, second)
                || isMethodOverride(first, second);
    }

    /**
     * Check if both types are ArrowTypeNode and
     * if the first type is a subtype of the second type.
     *
     * @param firstType  The first type
     * @param secondType The second type
     * @return True if the first type is a subtype of the second type, false otherwise
     */
    private static boolean isMethodOverride(final TypeNode firstType, final TypeNode secondType) {
        // Check if both types are ArrowTypeNode
        if (!(firstType instanceof ArrowTypeNode firstArrowTypeNode) ||
                !(secondType instanceof ArrowTypeNode secondArrowTypeNode)) {
            return false;
        }

        // Covariance of return type
        if (!isSubtype(firstArrowTypeNode.returnType, secondArrowTypeNode.returnType)) {
            return false;
        }

        // Contravariance of parameters
        for (int i = 0; i < firstArrowTypeNode.parameters.size(); i++) {
            if (!isSubtype(secondArrowTypeNode.parameters.get(i), firstArrowTypeNode.parameters.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the first type is a subclass of the second type.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a subclass of the second type, false otherwise
     */
    private static boolean isSubclass(final TypeNode first, final TypeNode second) {
        // Check if both types are RefTypeNode
        if (!(first instanceof RefTypeNode firstRefTypeNode)
                || !(second instanceof RefTypeNode secondRefTypeNode)) {
            return false;
        }
        // Check if the second type is a super type of the first type
        return getSuperTypeList(firstRefTypeNode.refClassId)
                .stream()
                .anyMatch(secondRefTypeNode.refClassId::equals);

    }


}
