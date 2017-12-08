package com.goldennode.testutils;

import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

public class RepeatableTestStatement extends Statement {
    private final int times;
    private final Statement statement;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RepeatableTestStatement.class);

    public RepeatableTestStatement(int times, Statement statement) {
        this.times = times;
        this.statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
        for (int i = 0; i < times; i++) {
            LOGGER.debug("Running Test " + (i + 1) + ". time");
            statement.evaluate();
        }
    }
}
