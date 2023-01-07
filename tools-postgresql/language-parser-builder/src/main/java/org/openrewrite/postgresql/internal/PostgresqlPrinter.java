package org.openrewrite.postgresql.internal;

import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;
import org.openrewrite.postgresql.PostgresqlVisitor;
import org.openrewrite.postgresql.tree.*;

import java.util.List;
import java.util.function.UnaryOperator;

public class PostgresqlPrinter<P> extends PostgresqlVisitor<PrintOutputCapture<P>> {
    private static final UnaryOperator<String> MARKER_WRAPPER =
            out -> "/*~~" + out + (out.isEmpty() ? "" : "~~") + ">*/";

    protected void beforeSyntax(Postgresql t, PrintOutputCapture<P> p) {
        beforeSyntax(t.getPrefix(), t.getMarkers(), p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), MARKER_WRAPPER));
        }
        visitSpace(prefix, p);
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Postgresql t, PrintOutputCapture<P> p) {
        afterSyntax(t.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), MARKER_WRAPPER));
        }
    }

    protected void visitRightPadded(List<? extends PostgresqlRightPadded<? extends Postgresql>> nodes, String suffixBetween, PrintOutputCapture<P> p) {
        for (int i = 0; i < nodes.size(); i++) {
            PostgresqlRightPadded<? extends Postgresql> node = nodes.get(i);
            visit(node.getElement(), p);
            visitSpace(node.getAfter(), p);
            visitMarkers(node.getMarkers(), p);
            if (i < nodes.size() - 1) {
                p.append(suffixBetween);
            }
        }
    }

    protected void visitContainer(String before, @Nullable PostgresqlContainer<? extends Postgresql> container,
                                  String suffixBetween, @Nullable String after, PrintOutputCapture<P> p) {
        if (container == null) {
            return;
        }
        beforeSyntax(container.getBefore(), container.getMarkers(), p);
        p.append(before);
        visitRightPadded(container.getPadding().getElements(), suffixBetween, p);
        afterSyntax(container.getMarkers(), p);
        p.append(after == null ? "" : after);
    }

    @Override
    public Space visitSpace(Space space, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    protected void visitLeftPadded(@Nullable String prefix, @Nullable PostgresqlLeftPadded<? extends Postgresql> leftPadded, PrintOutputCapture<P> p) {
        if (leftPadded != null) {
            beforeSyntax(leftPadded.getBefore(), leftPadded.getMarkers(), p);
            if (prefix != null) {
                p.append(prefix);
            }
            visit(leftPadded.getElement(), p);
            afterSyntax(leftPadded.getMarkers(), p);
        }
    }

    protected void visitRightPadded(@Nullable PostgresqlRightPadded<? extends Postgresql> rightPadded, @Nullable String suffix, PrintOutputCapture<P> p) {
        if (rightPadded != null) {
            beforeSyntax(Space.EMPTY, rightPadded.getMarkers(), p);
            visit(rightPadded.getElement(), p);
            afterSyntax(rightPadded.getMarkers(), p);
            visitSpace(rightPadded.getAfter(), p);
            if (suffix != null) {
                p.append(suffix);
            }
        }
    }

    public Postgresql visitDocument(Postgresql.Document document, PrintOutputCapture<P> p) {
        visitSpace(document.getPrefix(), p);
        visitMarkers(document.getMarkers(), p);
        visitContainer("", document.getPadding().getExpressions(), "", "", p);
        return document;
    }

    public Postgresql visitKeyValue(Postgresql.KeyValue keyValue, PrintOutputCapture<P> p) {
        visitSpace(keyValue.getPrefix(), p);
        visitMarkers(keyValue.getMarkers(), p);
        visitLeftPadded("", keyValue.getPadding().getValue(), p);
        return keyValue;
    }

    public Postgresql visitBareKey(Postgresql.BareKey bareKey, PrintOutputCapture<P> p) {
        visitSpace(bareKey.getPrefix(), p);
        visitMarkers(bareKey.getMarkers(), p);
        p.append(bareKey.getValue());
        return bareKey;
    }

    public Postgresql visitDottedKey(Postgresql.DottedKey dottedKey, PrintOutputCapture<P> p) {
        visitSpace(dottedKey.getPrefix(), p);
        visitMarkers(dottedKey.getMarkers(), p);
        visitContainer("", dottedKey.getPadding().getKeys(), "", "", p);
        return dottedKey;
    }

    public Postgresql visitLiteralString(Postgresql.LiteralString literalString, PrintOutputCapture<P> p) {
        visitSpace(literalString.getPrefix(), p);
        visitMarkers(literalString.getMarkers(), p);
        p.append(literalString.getValue());
        p.append(literalString.getValueSource());
        return literalString;
    }

    public Postgresql visitArray(Postgresql.Array array, PrintOutputCapture<P> p) {
        visitSpace(array.getPrefix(), p);
        visitMarkers(array.getMarkers(), p);
        visitContainer("", array.getPadding().getValues(), "", "", p);
        return array;
    }
}
