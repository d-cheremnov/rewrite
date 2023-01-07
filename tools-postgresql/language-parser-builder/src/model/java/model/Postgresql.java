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
package model;

import org.openrewrite.postgresql.tree.PostgresqlContainer;
import org.openrewrite.postgresql.tree.PostgresqlLeftPadded;

import java.nio.charset.Charset;
import java.nio.file.Path;

public interface Postgresql {

    class Document {
        Path sourcePath;
        Charset charset;
        PostgresqlContainer<Expression> expressions;
    }

    class KeyValue implements Expression {
        Key key;
        PostgresqlLeftPadded<TValue> value;
    }

    class BareKey implements Key {
        String value;
    }

    class DottedKey implements Key {
        PostgresqlContainer<Key> keys;
    }

    class LiteralString implements Key, TValue {
        String value;
        String valueSource;
    }

    class Array {
        PostgresqlContainer<TValue> values;
    }
}
