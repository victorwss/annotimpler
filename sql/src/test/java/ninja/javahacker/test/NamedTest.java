package ninja.javahacker.test;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.provider.Arguments;

public record NamedTest(String name, Executable exec) implements Executable {
    public Arguments args() {
        return Arguments.of(name, exec);
    }

    @Override
    public void execute() throws Throwable {
        exec.execute();
    }
}
