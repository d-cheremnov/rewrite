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

import org.intellij.lang.annotations.Language;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.postgresql.tree.Postgresql;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

public class Assertions {
    private Assertions() {
    }

    public static SourceSpecs postgresql(@Language("sql") @Nullable String before) {
        return Assertions.postgresql(before, s -> {
        });
    }

    public static SourceSpecs postgresql(@Language("sql") @Nullable String before, Consumer<SourceSpec<Postgresql.Documents>> spec) {
        SourceSpec<Postgresql.Documents> doc = new SourceSpec<>(Postgresql.Documents.class, null, PostgresqlParser.builder(), before, null);
        spec.accept(doc);
        return doc;
    }

    public static SourceSpecs postgresql(@Language("sql") @Nullable String before, @Language("sql") @Nullable String after) {
        return postgresql(before, after, s -> {
        });
    }

    public static SourceSpecs postgresql(@Language("sql") @Nullable String before, @Language("sql") @Nullable String after,
                                         Consumer<SourceSpec<Postgresql.Documents>> spec) {
        SourceSpec<Postgresql.Documents> doc = new SourceSpec<>(Postgresql.Documents.class, null, PostgresqlParser.builder(), before, s -> after);
        spec.accept(doc);
        return doc;
    }
}
