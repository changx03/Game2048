package org.changx03.boardGame2048;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by gungr on 1/12/2016.
 */
public class GridOperator {

    private static final List<Integer> traversalX = IntStream.range(0, 4).boxed().collect(Collectors.toList());
    private static final List<Integer> traversalY = IntStream.range(0, 4).boxed().collect(Collectors.toList());

    public static int traverseGrid(IntBinaryOperator func) {

        AtomicInteger at = new AtomicInteger();

        traversalX.forEach(x -> {
            traversalY.forEach(y -> {
                at.addAndGet(func.applyAsInt(x, y));
            });
        });

        return at.get();
    }

    public static void sortGrid(Direction direction){
        traversalX.sort(direction.equals(Direction.RIGHT) ? Collections.reverseOrder() : Integer::compareTo);
        traversalY.sort(direction.equals(Direction.DOWN) ? Collections.reverseOrder() : Integer::compareTo);
    }
}
