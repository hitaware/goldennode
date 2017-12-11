package com.goldennode.testutils;

import org.junit.Rule;

public class GoldenNodeJunitRunner {
    @Rule
    public TestCasePrinterRule pr = new TestCasePrinterRule(System.out);
    
    @Rule
    public RepeatedTestRule rule = new RepeatedTestRule();

 
}
