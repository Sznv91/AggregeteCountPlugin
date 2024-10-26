package com.tibbo.aggregate.common.expression;

import static java.util.concurrent.TimeUnit.HOURS;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.tibbo.aggregate.common.expression.parser.ASTStart;
import com.tibbo.aggregate.common.util.SyntaxErrorException;

/**
 * A wrapper around Guava's {@link LoadingCache} that stores parsed versions of AggreGate expressions, i.e. instances
 * of abstract syntax trees. The caching is based on the fact that for every expression text there is only one parsed
 * abstract syntax tree, so the text can be used as a key of the cache and the root of the tree can be a value.
 * @author Vladimir Plizga
 * @since AGG-16346
 * @see <a href="https://github.com/google/guava/wiki/CachesExplained">Guava Caches Explained</a>
 */
public class ParsedExpressionCache
{
    /**
     * The actual (underlying) cache. Prevents memory leaking by 2 means: keeping the total number of entries under a
     * limit and evicting old values after a long period of no use. Does not use weak nor soft references as it implies
     * comparison by {@code ==} rather than by {@code equals()} method which is not acceptable for current case. 
     */
    private static final LoadingCache<String, ASTStart> PARSE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(25, HOURS)       // one day and a bit more
            .recordStats()
            .build(new CacheLoader<String, ASTStart>() {
                @Override
                public ASTStart load(@Nonnull String expressionText) throws Exception {
                    ASTStart astStart = ExpressionUtils.parse(expressionText, true);
                    Evaluator.EvaluationStatistics.onExpressionParsed();
                    return astStart;
                }
            });
    /**
     * Tries to find corresponding parsed tree in the underlying cache and, if not found, transparently parses the 
     * expression, enters the tree into the cache and returns the tree as it was stored in the cache before.
     * @param expressionText the text of the expression to find corresponding AST for
     * @return the root of the parsed tree, either cached previously or freshly built
     * @throws SyntaxErrorException if the expression cannot be parsed due to a syntax error
     */
    public static ASTStart getCachedAstRoot(String expressionText) throws SyntaxErrorException
    {
        try
        {
            return PARSE_CACHE.getUnchecked(expressionText);
        }
        catch (ExecutionError | UncheckedExecutionException e)
        {
            if (e.getCause() instanceof SyntaxErrorException)
            {
                throw (SyntaxErrorException) e.getCause();
            } 
            else
            {
                throw e;
            }
        }
    }
}
