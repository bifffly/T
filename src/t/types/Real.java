package t.types;

public class Real {
    private double value;

    public Real(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String toString() {
        String text = String.valueOf(value);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Real) {
            return value == ((Real)o).value;
        }
        return false;
    }

    public static Real add(Real a, Real b) {
        return new Real(a.value + b.value);
    }

    public static Real multiply(Real a, Real b) {
        return new Real(a.value * b.value);
    }

    public static Real subtract(Real a, Real b) {
        return new Real(a.value - b.value);
    }

    public static Real divide(Real a, Real b) {
        return new Real(a.value / b.value);
    }

    public static Real mod(Real a, Real b) {
        return new Real(a.value % b.value);
    }

    public static Bool less(Real a, Real b) {
        return new Bool(a.value < b.value);
    }

    public static Bool lessEq(Real a, Real b) {
        return new Bool(a.value < b.value);
    }

    public static Bool greater(Real a, Real b) {
        return new Bool(a.value < b.value);
    }

    public static Bool greaterEq(Real a, Real b) {
        return new Bool(a.value < b.value);
    }
}
