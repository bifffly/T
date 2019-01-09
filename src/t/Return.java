package t;

public class Return extends RuntimeException {
    private final Object value;
    private final String type;

    public Return(Object value, String type) {
        super(null, null, false, false);
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
