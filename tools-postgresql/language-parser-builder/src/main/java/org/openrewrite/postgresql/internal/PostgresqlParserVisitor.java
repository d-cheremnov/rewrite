/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.postgresql.internal;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openrewrite.FileAttributes;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.internal.grammar.PostgreSQLParser;
import org.openrewrite.postgresql.internal.grammar.PostgreSQLParserBaseVisitor;
import org.openrewrite.postgresql.tree.Postgresql;
import org.openrewrite.postgresql.tree.PostgresqlContainer;
import org.openrewrite.postgresql.tree.PostgresqlRightPadded;
import org.openrewrite.postgresql.tree.Space;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.openrewrite.Tree.randomId;

public class PostgresqlParserVisitor extends PostgreSQLParserBaseVisitor<Postgresql> {
    private final Path path;

    @Nullable
    private final FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    public PostgresqlParserVisitor(
            Path path,
            @Nullable FileAttributes fileAttributes,
            String source,
            Charset charset,
            boolean charsetBomMarked
    ) {
        this.path = path;
        this.fileAttributes = fileAttributes;
        this.source = source;
        this.charset = charset;
        this.charsetBomMarked = charsetBomMarked;
    }

    @Override
    public Postgresql.Document visitRoot(PostgreSQLParser.RootContext ctx) {
        List<PostgresqlRightPadded<Postgresql>> list = new ArrayList<>();
        // The first element is the syntax, which we've already parsed
        // The last element is a "TerminalNode" which we are uninterested in
        for (int i = 0; i < ctx.children.size() - 1; i++) {
            Postgresql s = visit(ctx.children.get(i));
            PostgresqlRightPadded<Postgresql> protoProtoRightPadded = PostgresqlRightPadded.build(s).withAfter(
                    (s instanceof Postgresql.Document ||
                            s instanceof Postgresql.KeyValue ||
                            s instanceof Postgresql.BareKey ||
                            s instanceof Postgresql.DottedKey ||
                            s instanceof Postgresql.LiteralString ||
                            s instanceof Postgresql.Array
                    ) ? sourceBefore(";") : Space.EMPTY
            );
            list.add(protoProtoRightPadded);
        }
        PostgresqlContainer<Postgresql> expressions = PostgresqlContainer.build(Space.EMPTY, list, Markers.EMPTY);

        return convert(ctx, (c, prefix) -> new Postgresql.Document(
                randomId(),
                prefix,
                Markers.EMPTY,
                path,
                charset,
                charsetBomMarked,
                fileAttributes,
                null,
                expressions)
        );
    }

/*
    @Override
    public Postgresql visitStmt(PostgreSQLParser.StmtContext ctx) {
        String text = ctx.getText();
        //PostgreSQLParser.CreatestmtContext createstmt = ctx.createstmt();
        return new Postgresql.LiteralString(randomId(), Space.EMPTY, Markers.EMPTY, text, text);
    }

    @Override
    protected Postgresql defaultResult() {
        PostgresqlContainer<TValue> container = PostgresqlContainer.empty();
        return new Postgresql.Array(randomId(), Space.EMPTY, Markers.EMPTY, container);
    }

    @Override
    protected Postgresql aggregateResult(Postgresql aggregate, Postgresql nextResult) {
        Postgresql.Array array = (Postgresql.Array) aggregate;
        TValue value = nextResult.cast();
        array.getValues().add(value);
        return aggregate;
    }

    @Override
    public Postgresql visitCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {

        return new Postgresql.Array(randomId(), Space.EMPTY, Markers.EMPTY, null);
    }
*/

    private Space prefix(ParserRuleContext ctx) {
        return prefix(ctx.getStart());
    }

    private Space prefix(@Nullable TerminalNode terminalNode) {
        return terminalNode == null ? Space.EMPTY : prefix(terminalNode.getSymbol());
    }

    private Space prefix(Token token) {
        int start = token.getStartIndex();
        if (start < cursor) {
            return Space.EMPTY;
        }
        String prefix = source.substring(cursor, start);
        cursor = start;
        return Space.format(prefix);
    }

    @Nullable
    private <C extends ParserRuleContext, T> T convert(C ctx, BiFunction<C, Space, T> conversion) {
        //noinspection ConstantConditions
        if (ctx == null) {
            return null;
        }

        T t = conversion.apply(ctx, prefix(ctx));
        if (ctx.getStop() != null) {
            cursor = ctx.getStop().getStopIndex() + (Character.isWhitespace(source.charAt(ctx.getStop().getStopIndex())) ? 0 : 1);
        }

        return t;
    }

    private <T> T convert(TerminalNode node, BiFunction<TerminalNode, Space, T> conversion) {
        T t = conversion.apply(node, prefix(node));
        cursor = node.getSymbol().getStopIndex() + 1;
        return t;
    }

    private void skip(TerminalNode node) {
        cursor = node.getSymbol().getStopIndex() + 1;
    }

    /**
     * @return Source from <code>cursor</code> to next occurrence of <code>untilDelim</code>,
     * and if not found in the remaining source, the empty String. If <code>stop</code> is reached before
     * <code>untilDelim</code> return the empty String.
     */
    private Space sourceBefore(String untilDelim) {
        int delimIndex = positionOfNext(untilDelim, null);
        if (delimIndex < 0) {
            return Space.EMPTY; // unable to find this delimiter
        }

        String prefix = source.substring(cursor, delimIndex);
        cursor += prefix.length() + untilDelim.length(); // advance past the delimiter
        return Space.format(prefix);
    }

    private int positionOfNext(String untilDelim, @Nullable Character stop) {
        boolean inMultiLineComment = false;
        boolean inSingleLineComment = false;

        int delimIndex = cursor;
        for (; delimIndex < source.length() - untilDelim.length() + 1; delimIndex++) {
            if (inSingleLineComment) {
                if (source.charAt(delimIndex) == '\n') {
                    inSingleLineComment = false;
                }
            } else {
                if (source.length() - untilDelim.length() > delimIndex + 1) {
                    switch (source.substring(delimIndex, delimIndex + 2)) {
                        case "//":
                            inSingleLineComment = true;
                            delimIndex++;
                            break;
                        case "/*":
                            inMultiLineComment = true;
                            delimIndex++;
                            break;
                        case "*/":
                            inMultiLineComment = false;
                            delimIndex = delimIndex + 2;
                            break;
                    }
                }

                if (!inMultiLineComment && !inSingleLineComment) {
                    if (stop != null && source.charAt(delimIndex) == stop) {
                        return -1; // reached stop word before finding the delimiter
                    }

                    if (source.startsWith(untilDelim, delimIndex)) {
                        break; // found it!
                    }
                }
            }
        }

        return delimIndex > source.length() - untilDelim.length() ? -1 : delimIndex;
    }
}
