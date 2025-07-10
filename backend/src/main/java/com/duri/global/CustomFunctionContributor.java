package com.duri.global;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class CustomFunctionContributor implements FunctionContributor {

    private static final String FUNCTION_NAME = "match_against";
    private static final String FUNCTION_PATTERN =
        "match (?1, ?2, ?3, ?4) against (?5 in natural language mode)";

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
            .registerPattern(
                FUNCTION_NAME,
                FUNCTION_PATTERN,
                functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(DOUBLE)
            );
    }
}
