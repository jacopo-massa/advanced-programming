package framework;

import java.util.*;
import java.util.stream.Stream;
import utils.Pair;

public abstract class MapReduce<KIn, VIn, KMap, VMap, VOut> {

    // empty constructor
    public MapReduce() {
    }

    public final void init() {
        Stream<Pair<KIn, VIn>> inputStream = read();

        Stream<Pair<KMap, VMap>> mappedStream = map(inputStream);

        Stream<Pair<KMap, List<VMap>>> combinedStream = combine(mappedStream);

        Stream<Pair<KMap, VOut>> reducedStream = reduce(combinedStream);

        write(reducedStream);

        // write(reduce(combine(map(read()))));
    }

    private Stream<Pair<KMap, List<VMap>>> combine(Stream<Pair<KMap, VMap>> mappedStream) {

        Map<KMap, List<VMap>> combinedMap = new TreeMap<>(this::compare);

        mappedStream.forEach(pair -> {
            KMap key = pair.getKey();
            VMap value = pair.getValue();
            combinedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        });

        return combinedMap.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue()));

    }

    protected abstract Stream<Pair<KIn, VIn>> read();
    protected abstract Stream<Pair<KMap, VMap>> map(Stream<Pair<KIn, VIn>> inputStream);
    protected abstract int compare(KMap key1, KMap key2);
    protected abstract Stream<Pair<KMap, VOut>> reduce(Stream<Pair<KMap, List<VMap>>> combinedStream);
    protected abstract void write(Stream<Pair<KMap, VOut>> reducedStream);
}
