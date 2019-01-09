package t.includes;

import t.*;
import t.types.Function;

import java.util.ArrayList;
import java.util.List;

public class TPlot {
    private Environment env;
    private List<Function> functions = new ArrayList<>();

    public TPlot(Environment env) {
        this.env = env;
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
