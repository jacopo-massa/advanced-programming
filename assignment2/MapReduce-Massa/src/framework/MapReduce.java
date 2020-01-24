package framework;

import java.util.*;
import java.util.stream.Stream;
import utils.Pair;

public abstract class MapReduce<KIn, VIn, KMap, VMap, VOut> {

    // empty constructor
    public MapReduce() {
    }

    public final void init() {
        // verbose implementation

        // read data from a storage and converting them into key/value pairs.
        Stream<Pair<KIn, VIn>> inputStream = read();

        // process the input pairs and generate zero or more output key/value pairs.
        Stream<Pair<KMap, VMap>> mappedStream = map(inputStream);

        // group the input pairs by the key, generating output key/{list of values} pairs.
        Stream<Pair<KMap, List<VMap>>> combinedStream = combine(mappedStream);

        // iterate through the values that are associated with a key and produce zero or more output key/value pairs.
        Stream<Pair<KMap, VOut>> reducedStream = reduce(combinedStream);

        // write the output on a stable storage.
        write(reducedStream);


        // less readable way
        // write(reduce(combine(map(read()))));
    }

    // auxiliary function that groups pairs according to their key
    private Stream<Pair<KMap, List<VMap>>> combine(Stream<Pair<KMap, VMap>> mappedStream) {

        Map<KMap, List<VMap>> combinedMap = new TreeMap<>(this::compare);

        mappedStream.forEach(pair -> {
            KMap key = pair.getKey();
            VMap value = pair.getValue();
            /*
                if the key has never been processed, associate an empty list to that key.
                Add a value to the associated list, otherwise.
             */
            combinedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        });

        // returns a stream of key/{list of values} pairs.
        return combinedMap.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue()));

    }

    protected abstract Stream<Pair<KIn, VIn>> read();
    protected abstract Stream<Pair<KMap, VMap>> map(Stream<Pair<KIn, VIn>> inputStream);
    protected abstract int compare(KMap key1, KMap key2);
    protected abstract Stream<Pair<KMap, VOut>> reduce(Stream<Pair<KMap, List<VMap>>> combinedStream);
    protected abstract void write(Stream<Pair<KMap, VOut>> reducedStream);
}
