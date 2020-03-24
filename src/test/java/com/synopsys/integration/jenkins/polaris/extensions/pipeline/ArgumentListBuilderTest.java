package com.synopsys.integration.jenkins.polaris.extensions.pipeline;

import java.util.Arrays;
import java.util.stream.Collector;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.junit.jupiter.api.Test;

import hudson.Util;
import hudson.util.ArgumentListBuilder;
import hudson.util.QuotedStringTokenizer;

public class ArgumentListBuilderTest {

    @Test
    public void testConfigOverride() {
        final String pathToPolarisToolHome = "Some/path/to/Polaris.exe";
        final String windowsInputArguments = "--test-settings\r\n--co project={\\\"branch\\\":\\\"new branch\\\", \\\"name\\\":\\\"new name\\\"}\r\nanalyze";
        final String[] windowsInputArgumentArray = { "--test-settings", "--co", "project={\\\"branch\\\":\\\"new branch\\\", \\\"name\\\":\\\"new name\\\"}", "analyze" };
        final String linuxInputArguments = "--test-settings\r\n--co project='{\"branch\":\"new_branch\", \"name\":\"new_name\"}'\r\nanalyze";

        final Collector<String, ArgumentListBuilder, ArgumentListBuilder> toArgumentListBuilder = Collector.of(ArgumentListBuilder::new, ArgumentListBuilder::add, ArgumentListBuilder::add);

        System.out.println("# Util::tokenize");
        final ArgumentListBuilder tokenizeListBuilder = Arrays.stream(Util.tokenize(windowsInputArguments))
                                                            .peek(System.out::println)
                                                            .collect(toArgumentListBuilder);
        System.out.println();
        System.out.println(String.join(" ", pathToPolarisToolHome, tokenizeListBuilder.toString()));
        System.out.println();
        System.out.println();

        System.out.println("# QuotedStringTokenizer, no delim, yes quote");
        final QuotedStringTokenizer quotedStringTokenizer = new QuotedStringTokenizer(windowsInputArguments, " \t\n\r\f", false, true);
        final ArgumentListBuilder quotedTokenizeListBuilder = Arrays.stream(quotedStringTokenizer.toArray())
                                                                  .peek(System.out::println)
                                                                  .collect(toArgumentListBuilder);
        System.out.println();
        System.out.println(String.join(" ", pathToPolarisToolHome, quotedTokenizeListBuilder.toString()));
        System.out.println();
        System.out.println();

        System.out.println("# Strtokenizer");
        final StrTokenizer strTokenizer = new StrTokenizer(windowsInputArguments);
        strTokenizer.setQuoteMatcher(StrMatcher.quoteMatcher());
        final ArgumentListBuilder strTokenizeListBuilder = Arrays.stream(strTokenizer.getTokenArray())
                                                               .peek(System.out::println)
                                                               .collect(toArgumentListBuilder);
        System.out.println();
        System.out.println(String.join(" ", pathToPolarisToolHome, strTokenizeListBuilder.toString()));
        System.out.println();
        System.out.println();

        // This works //
        System.out.println("# Manually correct tokenizer");
        ArgumentListBuilder correctArgumentListBuilder = Arrays.stream(windowsInputArgumentArray)
                                                             .peek(System.out::println)
                                                             .collect(toArgumentListBuilder);
        System.out.println();
        // But how to tokenize correctly?

        System.out.println(String.join(" ", pathToPolarisToolHome, correctArgumentListBuilder.toString()));
    }
}
