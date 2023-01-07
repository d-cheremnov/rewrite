package org.openrewrite.postgresql.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.PostgresqlVisitor;
import org.openrewrite.postgresql.internal.PostgresqlPrinter;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Documents implements Postgresql, SourceFile {
        @EqualsAndHashCode.Include
        UUID id;

        Markers markers;
        Path sourcePath;

        @Nullable
        FileAttributes fileAttributes;

        @Nullable // for backwards compatibility
        @With(AccessLevel.PRIVATE)
        String charsetName;

        boolean charsetBomMarked;

        @Nullable
        Checksum checksum;

        @Override
        public Charset getCharset() {
            return charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName);
        }

        @Override
        public SourceFile withCharset(Charset charset) {
            return withCharsetName(charset.name());
        }

        List<? extends Document> documents;

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new PostgresqlPrinter<>();
        }
    }

@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Document implements Postgresql {
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
            return t.expressions == expressions ? t : new Document(t.padding, t.id, t.prefix, t.markers, t.sourcePath, t.charset, expressions);
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
