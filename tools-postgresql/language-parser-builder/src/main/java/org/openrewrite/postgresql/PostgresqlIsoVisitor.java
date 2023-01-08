/*
 * Copyright 2023. Asvoip team: Dmitry Cheremnov.
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
package org.openrewrite.postgresql;

import org.openrewrite.FileAttributes;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.internal.grammar.PostgreSQLParser;
import org.openrewrite.postgresql.tree.Postgresql;
import org.openrewrite.postgresql.tree.Space;

import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.openrewrite.Tree.randomId;

public class PostgresqlIsoVisitor<P> extends PostgresqlVisitor<P> {

    private final Path path;

    @Nullable
    private final FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    public PostgresqlIsoVisitor(Path path, @Nullable FileAttributes fileAttributes, String source, Charset charset, boolean charsetBomMarked) {
        this.path = path;
        this.fileAttributes = fileAttributes;
        this.source = source;
        this.charset = charset;
        this.charsetBomMarked = charsetBomMarked;
    }

    public Postgresql.Document visitDocument(PostgreSQLParser.Document_or_contentContext ctx, P p) {
        return new Postgresql.Document(
                randomId(),
                Space.EMPTY,
                Markers.EMPTY,
                path,
                charset,
                charsetBomMarked,
                fileAttributes,
                null,
                null
        );
    }

    @Override
    public Postgresql.Document visitDocument(Postgresql.Document document, P p) {
        return (Postgresql.Document) super.visitDocument(document, p);
    }

    @Override
    public Postgresql.KeyValue visitKeyValue(Postgresql.KeyValue keyValue, P p) {
        return (Postgresql.KeyValue) super.visitKeyValue(keyValue, p);
    }

    @Override
    public Postgresql.BareKey visitBareKey(Postgresql.BareKey bareKey, P p) {
        return (Postgresql.BareKey) super.visitBareKey(bareKey, p);
    }

    @Override
    public Postgresql.DottedKey visitDottedKey(Postgresql.DottedKey dottedKey, P p) {
        return (Postgresql.DottedKey) super.visitDottedKey(dottedKey, p);
    }

    @Override
    public Postgresql.LiteralString visitLiteralString(Postgresql.LiteralString literalString, P p) {
        return (Postgresql.LiteralString) super.visitLiteralString(literalString, p);
    }

    @Override
    public Postgresql.Array visitArray(Postgresql.Array array, P p) {
        return (Postgresql.Array) super.visitArray(array, p);
    }

}
