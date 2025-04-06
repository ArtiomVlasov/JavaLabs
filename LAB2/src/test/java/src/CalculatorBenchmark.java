package src;

import org.openjdk.jmh.annotations.*;
import src.context.Context;
import src.commands.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // Измеряем среднее время выполнения
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Выводим в миллисекундах
@State(Scope.Thread) // Создаём новое состояние для каждого потока
public class CalculatorBenchmark {

    private Context context;
    private PushCommand pushCommand;
    private PlusCommand plusCommand;
    private MinusCommand minusCommand;
    private MultiplicationCommand multiplicationCommand;
    private DivisionCommand divisionCommand;

    @Setup(Level.Iteration) // Вызывается перед каждой итерацией теста
    public void setup() {
        context = new Context();
        pushCommand = new PushCommand();
        plusCommand = new PlusCommand();
        minusCommand = new MinusCommand();
        multiplicationCommand = new MultiplicationCommand();
        divisionCommand = new DivisionCommand();
    }

    @Benchmark
    public void testPushOperation() {
        pushCommand.execute(context, "10");
    }

    @Benchmark
    public void testAdditionOperation() {
        pushCommand.execute(context, "10");
        pushCommand.execute(context, "20");
        plusCommand.execute(context);
    }

    @Benchmark
    public void testSubtractionOperation() {
        pushCommand.execute(context, "30");
        pushCommand.execute(context, "15");
        minusCommand.execute(context);
    }

    @Benchmark
    public void testMultiplicationOperation() {
        pushCommand.execute(context, "5");
        pushCommand.execute(context, "6");
        multiplicationCommand.execute(context);
    }

    @Benchmark
    public void testDivisionOperation() {
        pushCommand.execute(context, "50");
        pushCommand.execute(context, "5");
        divisionCommand.execute(context);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
