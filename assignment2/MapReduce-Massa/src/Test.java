public class Test {
    public static void main(String[] args) {

        final String test_path = "Books";

        WordCounter wordCounter = new WordCounter(test_path);
        wordCounter.init();
        System.out.println("Counting Words - Finished");

        InvertedIndex invertedIndex = new InvertedIndex(test_path);
        invertedIndex.init();
        System.out.println("Inverted Index - Finished");
    }
}
