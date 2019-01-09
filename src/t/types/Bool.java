package t.types;

public class Bool {
    private boolean value;

    public Bool(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Bool) {
            return value == ((Bool)o).value;
        }
        return false;
    }
}
