package org.openrewrite.postgresql;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.postgresql.Assertions.postgresql;

class PostgresqlParserTest implements RewriteTest {

    @Test
    void parsePostgresqlDocument() {
        rewriteRun(
                postgresql(
                        "CREATE TABLE hobbies_r (name		text, person 		text);"
                )
        );
    }

}