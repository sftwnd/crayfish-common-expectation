package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.common.expectation.IdentityHashCodes.sameIdentityHashCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IdentityOrderTest {

    @Test
    void sameObjectTest() {
        IdentityOrder order = new IdentityOrder();
        Object object = new Object();
        assertEquals(0, order.compare(object, object), "IdentityOrder::compare has to return zero for the same object");
        assertEquals(0, order.size(), "IdentityOrder::compare must not number an object compared with itself");
    }

    @Test
    void identityHashCodeTest() {
        IdentityOrder order = new IdentityOrder();
        Object first = new Object();
        Object second = new Object();
        while (System.identityHashCode(first) == System.identityHashCode(second)) {
            second = new Object();
        }
        int expected = Integer.signum(Integer.compare(System.identityHashCode(first), System.identityHashCode(second)));
        assertEquals(expected, Integer.signum(order.compare(first, second)), "IdentityOrder::compare has to order the objects by their identity hash codes");
        assertEquals(-expected, Integer.signum(order.compare(second, first)), "IdentityOrder::compare has to be antisymmetric");
        assertEquals(0, order.size(), "IdentityOrder::compare must not number objects of different identity hash codes");
    }

    @Test
    void sameIdentityHashCodeTest() {
        IdentityOrder order = new IdentityOrder();
        List<Object> objects = sameIdentityHashCode(Object::new);
        Object first = objects.get(0);
        Object second = objects.get(1);
        int result = order.compare(first, second);
        assertNotEquals(0, result, "IdentityOrder::compare must not make two objects of the same identity hash code equal");
        assertEquals(-result, order.compare(second, first), "IdentityOrder::compare has to be antisymmetric for objects of the same identity hash code");
        assertEquals(result, order.compare(first, second), "IdentityOrder::compare has to give the same order at every call");
        assertEquals(2, order.size(), "IdentityOrder::compare has to number each of two objects of the same identity hash code once");
    }

    @Test
    void collectedTest() {
        IdentityOrder order = new IdentityOrder();
        numberCollidingObjects(order);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (order.size() > 0 && System.nanoTime() < deadline) {
            System.gc();
        }
        assertEquals(0, order.size(), "IdentityOrder has to drop the numbers of the collected objects");
    }

    // the objects are referenced by this frame only, so that they can be collected after the call
    private static void numberCollidingObjects(IdentityOrder order) {
        List<Object> objects = sameIdentityHashCode(Object::new);
        order.compare(objects.get(0), objects.get(1));
        assertEquals(2, order.size(), "IdentityOrder::compare has to number two objects of the same identity hash code");
    }

}
