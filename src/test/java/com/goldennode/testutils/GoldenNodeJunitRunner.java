package com.goldennode.testutils;

import org.junit.Rule;

public class GoldenNodeJunitRunner {
    @Rule
    public PrinterRule pr = new PrinterRule(System.out);
    @Rule
    public RepeatedTestRule rule = new RepeatedTestRule();
}
