package t.types;

public class Char {
    private String value;

    public Char(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Char) {
            return value.equals(((Char)o).value);
        }
        return false;
    }
}
