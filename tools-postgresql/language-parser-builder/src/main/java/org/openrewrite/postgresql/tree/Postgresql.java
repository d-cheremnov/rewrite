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
package org.openrewrite.postgresql.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.PostgresqlVisitor;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface Postgresql extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptPostgresql(v.adapt(PostgresqlVisitor.class), p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(PostgresqlVisitor.class);
    }

    @Nullable
    default <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    default Space getPrefix() {
        return Space.EMPTY;
    }

    @ToString
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class Document implements Postgresql, SourceFile {
        @Nullable
        @NonFinal
        transient WeakReference<Padding> padding;

        @Getter
        @EqualsAndHashCode.Include
        @With
        UUID id;

        @Getter
        @With
        Space prefix;

        @Getter
        @With
        Markers markers;

        @Getter
        @With
        Path sourcePath;

        @Getter
        @With
        Charset charset;

        @Getter
        @With
        boolean charsetBomMarked;

        @Getter
        @With
        @Nullable
        FileAttributes fileAttributes;

        @Getter
        @With
        @Nullable
        Checksum checksum;

        PostgresqlContainer<Expression> expressions;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitDocument(this, p);
        }

        public List<Expression> getExpressions() {
            return expressions.getElements();
        }

        public Document withExpressions(List<Expression> expressions) {
            return getPadding().withExpressions(this.expressions.getPadding().withElements(PostgresqlRightPadded.withElements(
                    this.expressions.getPadding().getElements(), expressions)));
        }

        public Padding getPadding() {
            Padding p;
            if (this.padding == null) {
                p = new Padding(this);
                this.padding = new WeakReference<>(p);
            } else {
                p = this.padding.get();
                if (p == null || p.t != this) {
                    p = new Padding(this);
                    this.padding = new WeakReference<>(p);
                }
            }
            return p;
        }

        @RequiredArgsConstructor
        public static class Padding {
            private final Document t;

            public PostgresqlContainer<Expression> getExpressions() {
                return t.expressions;
            }

            public Document withExpressions(PostgresqlContainer<Expression> expressions) {
                return t.expressions == expressions ? t : new Document(t.padding, t.id, t.prefix, t.markers, t.sourcePath, t.charset, t.charsetBomMarked, t.fileAttributes, t.checksum, expressions);
            }
        }
    }

    @ToString
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class KeyValue implements Expression {
        @Nullable
        @NonFinal
        transient WeakReference<Padding> padding;

        @Getter
        @EqualsAndHashCode.Include
        @With
        UUID id;

        @Getter
        @With
        Space prefix;

        @Getter
        @With
        Markers markers;

        @Getter
        @With
        Key key;

        PostgresqlLeftPadded<TValue> value;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitKeyValue(this, p);
        }

        public TValue getValue() {
            return value.getElement();
        }

        public KeyValue withValue(TValue value) {
            //noinspection ConstantConditions
            return getPadding().withValue(PostgresqlLeftPadded.withElement(this.value, value));
        }

        public Padding getPadding() {
            Padding p;
            if (this.padding == null) {
                p = new Padding(this);
                this.padding = new WeakReference<>(p);
            } else {
                p = this.padding.get();
                if (p == null || p.t != this) {
                    p = new Padding(this);
                    this.padding = new WeakReference<>(p);
                }
            }
            return p;
        }

        @RequiredArgsConstructor
        public static class Padding {
            private final KeyValue t;

            public PostgresqlLeftPadded<TValue> getValue() {
                return t.value;
            }

            public KeyValue withValue(PostgresqlLeftPadded<TValue> value) {
                return t.value == value ? t : new KeyValue(t.padding, t.id, t.prefix, t.markers, t.key, value);
            }
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class BareKey implements Key {
        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String value;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitBareKey(this, p);
        }
    }

    @ToString
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class DottedKey implements Key {
        @Nullable
        @NonFinal
        transient WeakReference<Padding> padding;

        @Getter
        @EqualsAndHashCode.Include
        @With
        UUID id;

        @Getter
        @With
        Space prefix;

        @Getter
        @With
        Markers markers;

        PostgresqlContainer<Key> keys;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitDottedKey(this, p);
        }

        public List<Key> getKeys() {
            return keys.getElements();
        }

        public DottedKey withKeys(List<Key> keys) {
            return getPadding().withKeys(this.keys.getPadding().withElements(PostgresqlRightPadded.withElements(
                    this.keys.getPadding().getElements(), keys)));
        }

        public Padding getPadding() {
            Padding p;
            if (this.padding == null) {
                p = new Padding(this);
                this.padding = new WeakReference<>(p);
            } else {
                p = this.padding.get();
                if (p == null || p.t != this) {
                    p = new Padding(this);
                    this.padding = new WeakReference<>(p);
                }
            }
            return p;
        }

        @RequiredArgsConstructor
        public static class Padding {
            private final DottedKey t;

            public PostgresqlContainer<Key> getKeys() {
                return t.keys;
            }

            public DottedKey withKeys(PostgresqlContainer<Key> keys) {
                return t.keys == keys ? t : new DottedKey(t.padding, t.id, t.prefix, t.markers, keys);
            }
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class LiteralString implements Key, TValue {
        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String value;
        String valueSource;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitLiteralString(this, p);
        }
    }

    @ToString
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class Array implements Postgresql {
        @Nullable
        @NonFinal
        transient WeakReference<Padding> padding;

        @Getter
        @EqualsAndHashCode.Include
        @With
        UUID id;

        @Getter
        @With
        Space prefix;

        @Getter
        @With
        Markers markers;

        PostgresqlContainer<TValue> values;

        @Override
        public <P> Postgresql acceptPostgresql(PostgresqlVisitor<P> v, P p) {
            return v.visitArray(this, p);
        }

        public List<TValue> getValues() {
            return values.getElements();
        }

        public Array withValues(List<TValue> values) {
            return getPadding().withValues(this.values.getPadding().withElements(PostgresqlRightPadded.withElements(
                    this.values.getPadding().getElements(), values)));
        }

        public Padding getPadding() {
            Padding p;
            if (this.padding == null) {
                p = new Padding(this);
                this.padding = new WeakReference<>(p);
            } else {
                p = this.padding.get();
                if (p == null || p.t != this) {
                    p = new Padding(this);
                    this.padding = new WeakReference<>(p);
                }
            }
            return p;
        }

        @RequiredArgsConstructor
        public static class Padding {
            private final Array t;

            public PostgresqlContainer<TValue> getValues() {
                return t.values;
            }

            public Array withValues(PostgresqlContainer<TValue> values) {
                return t.values == values ? t : new Array(t.padding, t.id, t.prefix, t.markers, values);
            }
        }
    }
}
