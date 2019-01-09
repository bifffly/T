package t.types;

import t.Token;

import java.util.List;

public class Enum {
    private final Token name;
    private final List<Token> enums;

    public class EnumType {
        private final Enum e;
        private final Token name;

        private EnumType(Enum e, Token name) {
            this.e = e;
            this.name = name;
        }

        public Enum getEnum() {
            return e;
        }

        public Token getName() {
            return name;
        }

        @Override
        public String toString() {
            return "<enum element " + name.getLexeme() + ">";
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof EnumType) {
                return name.getLexeme().equals(((EnumType)o).name.getLexeme());
            }
            return false;
        }
    }

    public Enum(Token name, List<Token> enums) {
        this.name = name;
        this.enums = enums;
    }

    public Token getName() {
        return name;
    }

    public EnumType getEnum(Token name) {
        return new EnumType(this, name);
    }

    @Override
    public String toString() {
        return "<enum " + name.getLexeme() + ">";
    }
}
