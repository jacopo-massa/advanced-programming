import framework.MapReduce;
import utils.Pair;
import utils.Reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class InvertedIndex extends MapReduce<String, List<Pair<String, Integer>>, String, Pair<String, Integer>, Pair<String, Integer>> {

    // root path of the directory containing .txt files to analyze
    private final Path path;

    // Constructor of the class
    public InvertedIndex(String path) {
        this.path = Paths.get(path);
    }

    @Override
    protected Stream<Pair<String, List<Pair<String, Integer>>>> read() {
        try {
            // read data from a file, exploiting the provided Reader.
            Reader r = new Reader(this.path);
            return r.read().map(file -> {
                // for each file, a list of lines (Strings) and their number in that file (Integer) is generated.
                List<Pair<String, Integer>> lineList = new ArrayList<>();
                /* use AtomicInteger because the variable is updated into a lambda expression,
                   so it could be final or atomic.
                 */
                AtomicInteger lineNumber = new AtomicInteger();
                // for each line in a file, associate an integer number to it.
                file.getValue().forEach(line -> lineList.add(new Pair<>(line, lineNumber.getAndIncrement())));

                // return the Pair < filename, List of <lines, lineNumber> >)
                return new Pair<>(file.getKey(), lineList);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

    @Override
    protected Stream<Pair<String, Pair<String, Integer>>> map(Stream<Pair<String, List<Pair<String, Integer>>>> inputStream) {
        // for each file generate a stream on its lines
        return inputStream.map(file ->
                // for each line generate a stream on its words
                file.getValue().stream().map(line -> {
                    // split the line into words
                    String[] words = line.getKey().toUpperCase().replaceAll("[^A-Z0-9]", " ").split(" ");
                    return Arrays.stream(words)
                            // filter only words longer than 3 chars
                            .filter(word -> word.length() > 3)
                            // for each word generate the Pair <word, <filename, lineNumber>
                            .map(word -> new Pair<>(word, new Pair<>(file.getKey(), line.getValue())));
                }).flatMap(Function.identity()))
                .flatMap(Function.identity());
    }

    @Override
    protected int compare(String key1, String key2) {
        return key1.compareTo(key2);
    }

    @Override
    protected Stream<Pair<String, Pair<String, Integer>>> reduce(Stream<Pair<String, List<Pair<String, Integer>>>> combinedStream) {
        // for each word generate a stream on the lines containing it
        return combinedStream.map(word -> word.getValue().stream()
                // for each pair <word, List of <filename, lineNumber>>
                // generate a stream of Pairs: <word, <filename, line>> that can be accepted by the write function
                .map(file -> new Pair<>(word.getKey(), new Pair<>(file.getKey(), file.getValue()))))
                .flatMap(Function.identity());
    }

    @Override
    protected void write(Stream<Pair<String, Pair<String, Integer>>> reducedStream) {
        try {
            // write the reduced stream on a file, exploiting the auxiliary function implemented below
            writeIndex(new File("InvertedIndex.csv"), reducedStream);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // auxiliary function similar to the one in the Writer class, but with different types
    private static void writeIndex(File dst, Stream<Pair<String, Pair<String, Integer>>> stream) throws FileNotFoundException {
        PrintStream ps = new PrintStream(dst);
        stream.sorted(Comparator.comparing(Pair::getKey))
                .forEach(p -> ps.println(p.getKey() + ", " + p.getValue().getKey() + ", " + p.getValue().getValue()));
        ps.close();
    }
}
