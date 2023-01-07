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

import org.openrewrite.Cursor;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.tree.*;

import java.util.List;

public class PostgresqlVisitor<P> extends TreeVisitor<Postgresql, P> {

    public Space visitSpace(Space space, P p) {
        return space;
    }

    public <T> PostgresqlRightPadded<T> visitRightPadded(@Nullable PostgresqlRightPadded<T> right, P p) {
        if (right == null) {
            //noinspection ConstantConditions
            return null;
        }

        setCursor(new Cursor(getCursor(), right));

        T t = right.getElement();
        if (t instanceof J) {
            //noinspection unchecked
            t = visitAndCast((J) right.getElement(), p);
        }

        setCursor(getCursor().getParent());
        if (t == null) {
            //noinspection ConstantConditions
            return null;
        }

        Space after = visitSpace(right.getAfter(), p);
        Markers markers = visitMarkers(right.getMarkers(), p);
        return (after == right.getAfter() && t == right.getElement() && markers == right.getMarkers()) ?
                right : new PostgresqlRightPadded<>(t, after, markers);
    }

    public <T> PostgresqlLeftPadded<T> visitLeftPadded(@Nullable PostgresqlLeftPadded<T> left, P p) {
        if (left == null) {
            //noinspection ConstantConditions
            return null;
        }

        setCursor(new Cursor(getCursor(), left));

        Space before = visitSpace(left.getBefore(), p);
        T t = left.getElement();

        if (t instanceof J) {
            //noinspection unchecked
            t = visitAndCast((J) left.getElement(), p);
        }

        setCursor(getCursor().getParent());
        if (t == null) {
            // If nothing changed leave AST node the same
            if (left.getElement() == null && before == left.getBefore()) {
                return left;
            }
            //noinspection ConstantConditions
            return null;
        }

        return (before == left.getBefore() && t == left.getElement()) ? left : new PostgresqlLeftPadded<>(before, t, left.getMarkers());
    }

    public <T extends Postgresql> PostgresqlContainer<T> visitContainer(@Nullable PostgresqlContainer<T> container, P p) {
        if (container == null) {
            //noinspection ConstantConditions
            return null;
        }
        setCursor(new Cursor(getCursor(), container));

        Space before = visitSpace(container.getBefore(), p);
        List<PostgresqlRightPadded<T>> ts = ListUtils.map(container.getPadding().getElements(), t -> visitRightPadded(t, p));

        setCursor(getCursor().getParent());

        return ts == container.getPadding().getElements() && before == container.getBefore() ?
                container :
                PostgresqlContainer.build(before, ts, container.getMarkers());
    }

    public Postgresql visitDocument(Postgresql.Document document, P p) {
        Postgresql.Document d = document;
        d = d.withPrefix(visitSpace(d.getPrefix(), p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.getPadding().withExpressions(visitContainer(d.getPadding().getExpressions(), p));
        return d;
    }

    public Postgresql visitKeyValue(Postgresql.KeyValue keyValue, P p) {
        Postgresql.KeyValue k = keyValue;
        k = k.withPrefix(visitSpace(k.getPrefix(), p));
        k = k.withMarkers(visitMarkers(k.getMarkers(), p));
        k = k.getPadding().withValue(visitLeftPadded(k.getPadding().getValue(), p));
        return k;
    }

    public Postgresql visitBareKey(Postgresql.BareKey bareKey, P p) {
        Postgresql.BareKey b = bareKey;
        b = b.withPrefix(visitSpace(b.getPrefix(), p));
        b = b.withMarkers(visitMarkers(b.getMarkers(), p));

        return b;
    }

    public Postgresql visitDottedKey(Postgresql.DottedKey dottedKey, P p) {
        Postgresql.DottedKey d = dottedKey;
        d = d.withPrefix(visitSpace(d.getPrefix(), p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.getPadding().withKeys(visitContainer(d.getPadding().getKeys(), p));
        return d;
    }

    public Postgresql visitLiteralString(Postgresql.LiteralString literalString, P p) {
        Postgresql.LiteralString l = literalString;
        l = l.withPrefix(visitSpace(l.getPrefix(), p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));

        return l;
    }

    public Postgresql visitArray(Postgresql.Array array, P p) {
        Postgresql.Array a = array;
        a = a.withPrefix(visitSpace(a.getPrefix(), p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.getPadding().withValues(visitContainer(a.getPadding().getValues(), p));
        return a;
    }
}
