package com.tibbo.aggregate.common.expression.function.math;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class StandardErrorMeanFunction extends AbstractSingleValueCollectorFunction
{
    public StandardErrorMeanFunction() {
        super(new StandardDeviation(false), "standardError", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescStandardErrorMean"));
    }
}
