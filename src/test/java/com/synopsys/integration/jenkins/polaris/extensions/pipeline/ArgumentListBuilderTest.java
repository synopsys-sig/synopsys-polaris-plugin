package com.synopsys.integration.jenkins.polaris.extensions.pipeline;

import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collector;

import javax.annotation.Nonnull;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.junit.jupiter.api.Test;

import hudson.Util;
import hudson.util.ArgumentListBuilder;
import hudson.util.QuotedStringTokenizer;

public class ArgumentListBuilderTest {
    public static String HUDSON_UTILS_WHITESPACE_CHARACTERS = " \t\n\r\f";
    private final Collector<String, ArgumentListBuilder, ArgumentListBuilder> toArgumentListBuilder = Collector.of(ArgumentListBuilder::new, ArgumentListBuilder::add, ArgumentListBuilder::add);
    private final String pathToWindowsPolaris = "C:\\some\\path\\to\\polaris.exe";
    private final String pathToLinuxPolaris = "/some/path/to/polaris";
    private final String windowsInputArguments = "--test-settings\r\n--co project={\\\"branch\\\":\\\"new branch\\\", \\\"name\\\":\\\"new name\\\"}\r\nanalyze";
    private final String linuxInputArguments = "--test-settings\r\n--co project='{\"branch\":\"new_branch\", \"name\":\"new_name\"}'\r\nanalyze";
    private final String jenkinsInputArguments = "--test-settings\r\n--co project='{\"branch\":\"new_branch\", \"name\":\"new_name\"}'\r\nanalyze";
    private final String[] correctWindowsArguments = { "--test-settings", "--co", "project={\\\"branch\\\":\\\"new branch\\\", \\\"name\\\":\\\"new name\\\"}", "analyze" };
    private final String[] correctLinuxArguments = { "--test-settings", "--co", "project='{\"branch\":\"new branch\", \"name\":\"new name\"}'", "analyze" };
    private final String[] correctJenkinsArguments = { "--test-settings", "--co", "project={\"branch\":\"new branch\", \"name\":\"new name\"}", "analyze" };

    @Test
    public void testConfigOverride() {
        final Function<String, String[]> quotedStringTokenizer = arguments -> new QuotedStringTokenizer(arguments, HUDSON_UTILS_WHITESPACE_CHARACTERS, false, true).toArray();
        final Function<String, String[]> strTokenizer = arguments -> new StrTokenizer(arguments).setQuoteMatcher(StrMatcher.singleQuoteMatcher()).getTokenArray();

        System.out.println("# WINDOWS");
        System.out.println("## Util::tokenize");
        testWindowsTokenizer(Util::tokenize);

        System.out.println("## QuotedStringTokenizer, no return delim, yes return quote");
        // Delimiter based on Util::tokenize
        testWindowsTokenizer(quotedStringTokenizer);

        System.out.println("## Strtokenizer");
        testWindowsTokenizer(strTokenizer);

        System.out.println("## Manually correct tokenizer");
        testWindowsTokenizer(arguments -> correctWindowsArguments);

        System.out.println();
        System.out.println();
        System.out.println();

        System.out.println("# LINUX");
        System.out.println("## Util::tokenize");
        testLinuxTokenizer(Util::tokenize);

        System.out.println("## QuotedStringTokenizer, no return delim, yes return quote");
        // Delimiter based on Util::tokenize
        testLinuxTokenizer(quotedStringTokenizer);

        System.out.println("## Strtokenizer");
        testLinuxTokenizer(strTokenizer);

        System.out.println("## Manually correct tokenizer");
        testLinuxTokenizer(arguments -> correctLinuxArguments);
    }

    @Test
    public void testHandleEscaping() {
        System.out.println(tokenizeAndCreateListBuilder(jenkinsInputArguments, pathToLinuxPolaris));
        System.out.println(tokenizeAndCreateListBuilder(jenkinsInputArguments, pathToWindowsPolaris));
    }

    private void testLinuxTokenizer(final Function<String, String[]> tokenizer) {
        testTokenizer(tokenizer, pathToLinuxPolaris, linuxInputArguments);
    }

    private void testWindowsTokenizer(final Function<String, String[]> tokenizer) {
        testTokenizer(tokenizer, pathToWindowsPolaris, windowsInputArguments);
    }

    private void testTokenizer(final Function<String, String[]> tokenizer, final String polarisExecutable, final String argument) {
        final ArgumentListBuilder tokenizeListBuilder = Arrays.stream(tokenizer.apply(argument))
                                                            .peek(System.out::println)
                                                            .collect(toArgumentListBuilder)
                                                            .prepend(polarisExecutable);
        System.out.println();
        System.out.println(tokenizeListBuilder.toString());
        System.out.println();
        System.out.println();

    }

    private ArgumentListBuilder tokenizeAndCreateListBuilder(final String polarisArguments, final String pathToPolarisCli) {
        //final QuotedStringTokenizer quotedStringTokenizer = new QuotedStringTokenizer(polarisArguments, " \t\n\r\f", false, true);
        //final ArgumentListBuilder argumentListBuilder = new ArgumentListBuilder();
        //boolean isJson = false;
        //for (final String argument : quotedStringTokenizer.toArray()) {
        //if (isJson) {
        //    argumentListBuilder.add(StringEscapeUtils.escapeJson(argument));
        //} else {
        // argumentListBuilder.add(argument);
        //}
        //isJson = ("--co".equals(argument));
        //}
        final ArgumentListBuilder argumentListBuilder = new ArgumentListBuilder(pathToPolarisCli);
        argumentListBuilder.addTokenized(polarisArguments);
        return argumentListBuilder;
    }

    private String[] tokenizeCustom(@Nonnull final String arguments) {
        StringTokenizer stringTokenizer = new StringTokenizer(arguments);
        if (arguments.contains("--co")) {
            final int overrideStartsAt = arguments.indexOf("--co");
            final String[] argsBeforeOverride = Util.tokenize(arguments.substring(0, overrideStartsAt));
        } else {
            return Util.tokenize(arguments);
        }

        String unparsedArguments = arguments;
        while (unparsedArguments.length() > 0) {

        }
        for (String argument : arguments.split("[" + HUDSON_UTILS_WHITESPACE_CHARACTERS + "]+")) {

        }
        return (String[]) Collections.emptyList().toArray();
    }
}
