package org.openrewrite.postgresql;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.postgresql.Assertions.postgresql;

class PostgresqlParserTest implements RewriteTest {

    @Test
    void parsePostgresqlDocument1() {
        rewriteRun(
                postgresql(
                        "CREATE TABLE hobbies_r (name		text, person 		text);"
                )
        );
    }

    @Test
    void parsePostgresqlDocument2() {
        rewriteRun(
                postgresql(
                        "CREATE TABLE IF NOT EXISTS hobbies_r (name		text, person 		text);"
                )
        );
    }

}