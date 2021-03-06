import framework.MapReduce;
import utils.Pair;
import utils.Reader;
import utils.Writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class WordCounter extends MapReduce<String, List<String>, String, Integer, Integer> {

    // root path of the directory containing .txt files to analyze
    private final Path path;

    // Constructor of the class
    public WordCounter(String path) {
        this.path = Paths.get(path);
    }

    @Override
    protected Stream<Pair<String, List<String>>> read() {
        // read data from a file, exploiting the provided Reader.
        try {
            return new Reader(this.path).read();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

    @Override
    protected Stream<Pair<String, Integer>> map(Stream<Pair<String, List<String>>> inputStream) {
        return inputStream.map(
                // get a stream for each line generated by 'read' method
                pair -> pair.getValue().stream().map(line -> {
                    // Clean line from non-alphabetic chars, and split in words
                    String[] words = line.toUpperCase().replaceAll("[^A-Z0-9]", " ").split(" ");
                    Map<String, Pair<String, Integer>> counters = new TreeMap<>(String::compareTo);
                    // Count occurrences of each word in that line
                    // and saving them into an array
                    Arrays.stream(words)
                            .filter(word -> word.length() > 3) // filter only words longer than 3 chars
                            .forEach(word ->
                                counters.put(word, new Pair<>(word, counters.getOrDefault(word, new Pair<>(word,0)).getValue()+1)));

                    return counters.values().stream();
                }).flatMap(Function.identity())) // compress the streams, each generated by mapping the lines in a file (call them 'S_i')
                .flatMap(Function.identity()); // compress all the streams 'S_i' into a single stream.
    }

    @Override
    protected int compare(String key1, String key2) {
        return key1.compareTo(key2);
    }

    @Override
    protected Stream<Pair<String, Integer>> reduce(Stream<Pair<String, List<Integer>>> combinedStream) {
        return combinedStream.map(pair -> {
            String key = pair.getKey();
            // substitute the list of occurrences for a word with their sum.
            Integer value = pair.getValue().stream().reduce(0, Integer::sum);
            return new Pair<>(key,value);
        });
    }

    @Override
    protected void write(Stream<Pair<String, Integer>> reducedStream) {
        try {
            // write the reduced stream on a file, exploiting the provided Writer
            Writer.write(new File("CountedWords.csv"), reducedStream);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
