package uk.co.probablyfine.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import uk.co.probablyfine.matchers.function.DescribableFunction;

import java.lang.reflect.Method;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.co.probablyfine.matchers.ApiHelper.describe;
import static uk.co.probablyfine.matchers.ApiHelper.isDeprecated;
import static uk.co.probablyfine.matchers.HamcrestApiMatchers.existsInHamcrest;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class StreamMatchersTest {

    @Test
    void equalTo_failureDifferingSingleItem() {
        assertThat(Stream.of("a"), is(not(StreamMatchers.yieldsSameAs(Stream.of("b")))));
    }

    @Test
    void contains_failureDifferingSingleItem() {
        assertThat(Stream.of("a"), not(StreamMatchers.yieldsExactly("b")));
    }

    @Test
    void equalTo_failureDifferingLength() {
        assertThat(Stream.of("a"), not(StreamMatchers.yieldsSameAs(Stream.of("a", "b"))));
    }

    @Test
    void contains_failureDifferingLength() {
        assertThat(Stream.of("a"), not(StreamMatchers.yieldsExactly("a", "b")));
    }

    @Test
    void equalTo_failureDifferingItems() {
        assertThat(Stream.of("a","c"), not(StreamMatchers.yieldsSameAs(Stream.of("a", "b"))));
    }

    @Test
    void contains_failureDifferingItems() {
        assertThat(Stream.of("a","c"), not(StreamMatchers.yieldsExactly("a", "b")));
    }

    @Test
    void equalTo_successEmpty() {
        assertThat(Stream.empty(), StreamMatchers.yieldsSameAs(Stream.empty()));
    }

    @Test
    void empty_Success() {
        assertThat(Stream.empty(), StreamMatchers.yieldsNothing());
    }

    @Test
    void empty_Failure() {
        Helper.testFailingMatcher(Stream.of(3), StreamMatchers.yieldsNothing(),
                "A Stream yielding no elements", "the Stream started with <3> and is then exhausted");
        Helper.testFailingMatcher(IntStream.iterate(0, i -> i + 1), StreamMatchers.yieldsNothing(),
                "A Stream yielding no elements", "the Stream started with <0> and will yield even more elements");
    }

    @Test
    void equalToIntStream_success() {
        assertThat(IntStream.range(1, 10), StreamMatchers.yieldsSameAs(IntStream.range(1, 10)));
    }

    @Test
    void containsIntStream_success() {
        assertThat(IntStream.range(1, 4), StreamMatchers.yieldsExactly(1, 2, 3));
    }

    @Test
    void equalTo_successManyItems() {
        assertThat(Stream.of("a", "b", "c"), StreamMatchers.yieldsSameAs(Stream.of("a", "b", "c")));
    }

    @Test
    void contains_successManyItems() {
        assertThat(Stream.of("a", "b", "c"), StreamMatchers.yieldsExactly("a", "b", "c"));
    }

    @Test
    void contains_is_nullsafe() {
        assertThat(Stream.of("a", null, "c"), StreamMatchers.yieldsExactly("a", null, "c"));
    }

    @Test
    void allMatch_success() {
        assertThat(Stream.of("bar","baz"), StreamMatchers.allMatch(containsString("a")));
    }

    @Test
    void allMatch_failure() {
        Matcher<Stream<String>> matcher = StreamMatchers.allMatch(containsString("a"));
        Stream<String> testData = Stream.of("bar", "bar", "foo", "grault", "garply", "waldo");
        Helper.testFailingMatcher(testData, matcher, "All to match <a string containing \"a\">", "Item 2 failed to match: \"foo\"");
    }

    @Test
    void allMatchInt_failure() {
        Matcher<IntStream> matcher = StreamMatchers.allMatchInt(Matchers.lessThan(3));
        IntStream testData = IntStream.range(0, 10);
        Helper.testFailingMatcher(testData, matcher, "All to match <a value less than <3>>", "Item 3 failed to match: <3>");
    }

    @Test
    void allMatchLong_failure() {
        Matcher<LongStream> matcher = StreamMatchers.allMatchLong(Matchers.lessThan(3L));
        LongStream testData = LongStream.range(0, 10);
        Helper.testFailingMatcher(testData, matcher, "All to match <a value less than <3L>>", "Item 3 failed to match: <3L>");
    }

    @Test
    void allMatchDouble_failure() {
        Matcher<DoubleStream> matcher = StreamMatchers.allMatchDouble(Matchers.lessThan(3d));
        DoubleStream testData = DoubleStream.iterate(0d, d -> d + 1).limit(10);
        Helper.testFailingMatcher(testData, matcher, "All to match <a value less than <3.0>>", "Item 3 failed to match: <3.0>");
    }

    @Test
    void allMatch_empty() {
        assertThat(Stream.empty(), StreamMatchers.allMatch(containsString("foo")));
    }

    @Test
    void anyMatch_success() {
        assertThat(Stream.of("bar", "bar", "foo", "grault", "garply", "waldo"), StreamMatchers.anyMatch(containsString("ald")));
    }

    @Test
    void anyMatch_failure() {
        Matcher<Stream<String>> matcher = StreamMatchers.anyMatch(containsString("z"));
        Stream<String> testData = Stream.of("bar", "bar", "foo", "grault", "garply", "waldo");
        Helper.testFailingMatcher(testData, matcher, "Any to match <a string containing \"z\"", "None of these items matched: [\"bar\",\"bar\",\"foo\",\"grault\",\"garply\",\"waldo\"]");
    }

    @Test
    void anyMatchInt_success() {
        assertThat(IntStream.range(0, 1_000), StreamMatchers.anyMatchInt(equalTo(10)));
    }

    @Test
    void anyMatchInt_failure() {
        Helper.testFailingMatcher(IntStream.range(0, 5), StreamMatchers.anyMatchInt(equalTo(101)), "Any to match <<101>>", "None of these items matched: [<0>,<1>,<2>,<3>,<4>]");
    }

    @Test
    void anyMatchLong_success() {
        assertThat(LongStream.range(0, 1_000), StreamMatchers.anyMatchLong(equalTo(10L)));
    }

    @Test
    void anyMatchLong_failure() {
        Helper.testFailingMatcher(LongStream.range(0, 5), StreamMatchers.anyMatchLong(equalTo(101L)), "Any to match <<101L>>", "None of these items matched: [<0L>,<1L>,<2L>,<3L>,<4L>]");
    }

    @Test
    void anyMatchDouble_success() {
        assertThat(DoubleStream.iterate(0d, i -> i + 1), StreamMatchers.anyMatchDouble(equalTo(10d)));
    }

    @Test
    void anyMatchDouble_failure() {
        Helper.testFailingMatcher(DoubleStream.iterate(0d, i -> i + 1).limit(5), StreamMatchers.anyMatchDouble(equalTo(101d)), "Any to match <<101.0>>", "None of these items matched: [<0.0>,<1.0>,<2.0>,<3.0>,<4.0>]");
    }

    @Test
    void anyMatch_empty() {
        assertThat(Stream.empty(),not(StreamMatchers.anyMatch(containsString("foo"))));
    }

    @Test
    void startsWithMatcher_success() {
        assertThat(Stream.iterate(0, i -> i + 1), StreamMatchers.startsWith(Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 10));
    }

    @Test
    void startsWithMatcher_successBothInfinite() {
        assertThat(Stream.iterate(0,i -> i + 1), StreamMatchers.startsWith(Stream.iterate(0, i -> i + 1), 10));
    }

    @Test
    void startsWithMatcherInt_successBothInfinite() {
        assertThat(IntStream.iterate(0, i -> i + 1), StreamMatchers.startsWith(IntStream.iterate(0, i -> i + 1), 10));
    }

    @Test
    void startsWithMatcherLong_successBothInfinite() {
        assertThat(LongStream.iterate(0, i -> i + 1), StreamMatchers.startsWith(LongStream.iterate(0, i -> i + 1), 10));
    }

    @Test
    void startsWithMatcherDouble_successBothInfinite() {
        assertThat(DoubleStream.iterate(0, i -> i + 1), StreamMatchers.startsWith(DoubleStream.iterate(0, i -> i + 1), 10));
    }


    @Test
    void startsWithItems_success() {
        assertThat(Stream.of("a", "b", "c", "d", "e", "f", "g", "h"), StreamMatchers.startsWith("a", "b", "c", "d", "e"));
    }

    @Test
    void startsWithItemsIntStream_success() {
        assertThat(IntStream.range(0, Integer.MAX_VALUE), StreamMatchers.startsWithInt(0, 1, 2, 3, 4));
    }

    @Test
    void equalTo_failureMessages() {
        Matcher<Stream<String>> matcher = StreamMatchers.yieldsSameAs(Stream.of("a", "b", "c", "d", "e", "f", "g", "h"));
        Stream<String> testData = Stream.of("a", "b", "c", "d", "e");
        Helper.testFailingMatcher(testData, matcher, "Stream of [\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\"]", "Stream of [\"a\",\"b\",\"c\",\"d\",\"e\"]");
    }

    @Test
    public void equalTo_handles_types() {
        Stream<Character> expectedStream = Stream.of('x', 'y', 'z');
        assertThat("xyz", where(s -> s.chars().mapToObj(i -> (char) i), StreamMatchers.yieldsSameAs(expectedStream)));

        BaseStream<Character, Stream<Character>> expectedBaseStream = Stream.of('x', 'y', 'z');
        assertThat("xyz", where(s -> s.chars().mapToObj(i -> (char) i), StreamMatchers.yieldsSameAs(expectedBaseStream)));

        DescribableFunction<String, BaseStream<Character, Stream<Character>>> characters = s -> s.chars().mapToObj(i -> (char) i);
        assertThat("xyz", where(characters, StreamMatchers.yieldsSameAs(Stream.of('x', 'y', 'z'))));
    }

    @Test
    public void contains_handles_types() {
        assertThat("xyz", where(s -> s.chars().mapToObj(i -> (char) i), StreamMatchers.yieldsExactly('x', 'y', 'z')));

        DescribableFunction<String, BaseStream<Character, Stream<Character>>> characters = s -> s.chars().mapToObj(i -> (char) i);
        assertThat("xyz", where(characters, StreamMatchers.yieldsExactly('x', 'y', 'z')));
        assertThat("xyz", where(characters, not(StreamMatchers.yieldsExactly('x', 'y'))));
    }


    @Test
    void contains_failureMessages() {
        Stream<String> testData = Stream.of("a", "b", "c", "d", "e");
        Matcher<Stream<String>> matcher = StreamMatchers.yieldsExactly("a", "b", "c", "d", "e", "f", "g", "h");
        Helper.testFailingMatcher(testData, matcher, "Stream of [\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\"]", "Stream of [\"a\",\"b\",\"c\",\"d\",\"e\"]");
    }

    @Test
    void equalToIntStream_failureMessages() {
        IntStream testData = IntStream.range(8, 10);
        Matcher<IntStream> matcher = StreamMatchers.yieldsSameAs(IntStream.range(0, 6));
        Helper.testFailingMatcher(testData, matcher, "Stream of [<0>,<1>,<2>,<3>,<4>,<5>]", "Stream of [<8>,<9>]");
    }

    @Test
    void startsWithAll_success() {
        assertThat(Stream.generate(() -> 10), StreamMatchers.startsWithAll(equalTo(10),100));
    }

    @Test
    void startsWithAll_fail() {
        Helper.testFailingMatcher(Stream.generate(() -> 11), StreamMatchers.startsWithAll(equalTo(10), 100), "First 100 to match <<10>>", "Item 0 failed to match: <11>");
    }

    @Test
    void startsWithAllInt_success() {
        assertThat(IntStream.generate(() -> 10), StreamMatchers.startsWithAllInt(equalTo(10), 100));
    }

    @Test
    void startsWithAllInt_fail() {
        Helper.testFailingMatcher(IntStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAllInt(Matchers.lessThan(3), 100), "First 100 to match <a value less than <3>>", "Item 3 failed to match: <3>");
    }

    @Test
    void startsWithAllLong_success() {
        assertThat(LongStream.generate(() -> 10), StreamMatchers.startsWithAllLong(equalTo(10L), 100));
    }

    @Test
    void startsWithAllLong_fail() {
        Helper.testFailingMatcher(LongStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAllLong(Matchers.lessThan(3L), 100), "First 100 to match <a value less than <3L>>", "Item 3 failed to match: <3L>");
    }

    @Test
    void startsWithAllDouble_success() {
        assertThat(DoubleStream.generate(() -> 10), StreamMatchers.startsWithAllDouble(equalTo(10d), 100));
    }

    @Test
    void startsWithAllDouble_fail() {
        Helper.testFailingMatcher(DoubleStream.iterate(0,i -> i + 1), StreamMatchers.startsWithAllDouble(Matchers.lessThan(3d), 100), "First 100 to match <a value less than <3.0>>", "Item 3 failed to match: <3.0>");
    }

    @Test
    void startsWithAny_success() {
        assertThat(Stream.iterate(0, i -> i + 1), StreamMatchers.startsWithAny(equalTo(10), 100));
    }

    @Test
    void startsWithAny_fail() {
        Helper.testFailingMatcher(Stream.iterate(0, i -> i + 1), StreamMatchers.startsWithAny(equalTo(-1), 10), "Any of first 10 to match <<-1>>", "None of these items matched: [<0>,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>]");
    }

    @Test
    void startsWithAnyInt_success() {
        assertThat(IntStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyInt(equalTo(10), 100));
    }

    @Test
    void startsWithAnyInt_fail() {
        Helper.testFailingMatcher(IntStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyInt(equalTo(-1), 10), "Any of first 10 to match <<-1>>", "None of these items matched: [<0>,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>]");
    }

    @Test
    void startsWithAnyLong_success() {
        assertThat(LongStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyLong(equalTo(10L), 100));
    }

    @Test
    void startsWithAnyLong_fail() {
        Helper.testFailingMatcher(LongStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyLong(equalTo(-1L), 10), "Any of first 10 to match <<-1L>>", "None of these items matched: [<0L>,<1L>,<2L>,<3L>,<4L>,<5L>,<6L>,<7L>,<8L>,<9L>]");
    }

    @Test
    void startsWithAnyDouble_success() {
        assertThat(DoubleStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyDouble(equalTo(10d), 100));
    }

    @Test
    void startsWithAnyDouble_fail() {
        Helper.testFailingMatcher(DoubleStream.iterate(0, i -> i + 1), StreamMatchers.startsWithAnyDouble(equalTo(-1d), 10), "Any of first 10 to match <<-1.0>>", "None of these items matched: [<0.0>,<1.0>,<2.0>,<3.0>,<4.0>,<5.0>,<6.0>,<7.0>,<8.0>,<9.0>]");
    }

    @Test
    void contains_returnsParameterizedMatcher() {
        usesStreamMatcher(Stream.of(10), StreamMatchers.yieldsExactly(10));
    }

    @Test
    void contains_acceptsMatchers() {
        usesStreamMatcher(
            Stream.of(10, 20, 30),
            StreamMatchers.yieldsExactly(
                is(10),
                lessThanOrEqualTo(20),
                not(20)
            )
        );
    }

    @Test
    void noNonDeprecatedMatchersNameClashWithHamcrestMatchers() {
        assertAll(Stream.of(StreamMatchers.class.getMethods())
                .filter(method -> !isDeprecated(method)).sorted(comparing(Method::getName))
                .map(method -> () -> assertThat(method, not(existsInHamcrest()))));
    }

    @Test
    void relatedMatchersOfDeprecatedMatchersAreAlsoDeprecated() {
        ApiInspector streamMatchersApi = new ApiInspector(StreamMatchers.class, ApiHelper::isMatcherMethod);
        assertAll(streamMatchersApi.getDeprecated()
                .flatMap(streamMatchersApi::findRelatedOf).distinct()
                .map(relatedMethod -> () -> assertThat(describe(relatedMethod) + " is deprecated", relatedMethod, where(ApiHelper::isDeprecated))));
    }


    private static void usesStreamMatcher(Stream<Integer> stream, Matcher<Stream<Integer>> matcher) {
        assertThat(stream, matcher);
    }
}
