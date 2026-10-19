package com.github.sftwnd.crayfish.common.expectation;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The type argument of {@link Comparable} as a class implements it: the class of the objects that the
 * {@code compareTo} of the class accepts, resolved along the generic supertypes of the class.
 */
final class ComparableArgument {

    private ComparableArgument() {
    }

    /**
     * Returns the erasure of the type argument of {@code Comparable} as a class implements it, itself or by a
     * superclass or an interface: {@code A} for {@code Comparable<A>}, with the type variables of the generic
     * supertypes substituted, and {@code Object} for a raw {@code Comparable}.
     *
     * @param type a class that implements {@code Comparable}
     * @return the class that the {@code compareTo} of the class accepts
     */
    static Class<?> of(Class<?> type) {
        return of(type, Collections.emptyMap());
    }

    // the argument for a class whose type variables have the given values
    private static Class<?> of(Class<?> type, Map<TypeVariable<?>, Type> values) {
        // the superclass or the interface on the way to Comparable
        Type supertype = Stream.concat(Stream.of(type.getGenericSuperclass()), Arrays.stream(type.getGenericInterfaces()))
                .filter(Objects::nonNull)
                .filter(candidate -> Comparable.class.isAssignableFrom(erasure(candidate)))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        Class<?> raw = erasure(supertype);
        if (raw != Comparable.class) {
            return of(raw, values(raw, supertype, values));
        }
        return supertype instanceof ParameterizedType
                ? erasure(value(((ParameterizedType) supertype).getActualTypeArguments()[0], values))
                : Object.class;
    }

    // the values of the type variables of a supertype, as the subtype names it
    private static Map<TypeVariable<?>, Type> values(Class<?> raw, Type supertype, Map<TypeVariable<?>, Type> values) {
        Map<TypeVariable<?>, Type> result = new HashMap<>();
        if (supertype instanceof ParameterizedType) {
            TypeVariable<?>[] variables = raw.getTypeParameters();
            Type[] arguments = ((ParameterizedType) supertype).getActualTypeArguments();
            for (int i = 0; i < variables.length; i++) {
                result.put(variables[i], value(arguments[i], values));
            }
        }
        return result;
    }

    // a type, or the value of a type variable
    private static Type value(Type type, Map<TypeVariable<?>, Type> values) {
        Type value = values.get(type);
        return value != null ? value : type;
    }

    // the class of a type: a type variable without a value is erased to its first bound
    private static Class<?> erasure(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof TypeVariable) {
            return erasure(((TypeVariable<?>) type).getBounds()[0]);
        }
        // a generic array, such as T[]: a wildcard cannot be the argument of a supertype
        return Array.newInstance(erasure(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
    }

}
