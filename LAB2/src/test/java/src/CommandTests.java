package src;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import src.commands.*;
import src.context.Context;

import static org.junit.jupiter.api.Assertions.*;


public class CommandTests{
    private final Context context = new Context();

    @Nested
    @DisplayName("Stack operations Test")
    public class StackTests{
        @Test
        @DisplayName("Pop non empty stack test")
        public void testPopCommand_NonEmptyStack(){
            Command Pop = new PopCommand();
            context.getStack().push(1.2);
            context.getStack().push(1.3);
            Pop.execute(context);
            assertFalse(context.getStack().isEmpty());
        }

        @Test
        @DisplayName("Pop empty stack test")
        public void testPopCommand_EmptyStack(){
            Command Pop = new PopCommand();
            Pop.execute(context);
            assertTrue(context.getStack().isEmpty());
        }

        @Test
        @DisplayName("Push valid value test")
        public void testPushCommand_ValidArguments(){
            String[] args1 = new String[]{"1.2"};
            String[] args2 = new String[]{"1.3"};
            Command Push = new PushCommand();
            Push.execute(context, args1);
            Push.execute(context, args2);
            assertEquals(1.3, context.getStack().get(1));

        }

        @Test
        @DisplayName("Push non valid value test")
        public void testPushCommand_InvalidArguments(){
            String[] args1 = new String[]{"rfgg"};
            Command Push = new PushCommand();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Push.execute(context, args1));
            assertEquals(("Unknown parameter: " + args1[0]), exception.getMessage());
        }

        @Test
        @DisplayName("Push invalid argument length test")
        public void testPushCommand_InvalidArgumentsLength(){
            String[] args1 = new String[]{};
            Command Push = new PushCommand();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Push.execute(context, args1));
            assertEquals("Bad arguments for PUSH.\nPUSH command requires one argument", exception.getMessage());
        }

        @Test
        @DisplayName("Define valid argument test")
        public void testDefineCommand_ValidArgumentsTest(){
            String[] def1 = new String[]{"a", "2"};
            Command Define = new DefineCommand();
            Define.execute(context, def1);
            assertTrue(context.getVars().containsKey("a"));
        }

        @Test
        @DisplayName("Define invalid argument test")
        public void testDefineCommand_InvalidArgumentsTest(){
            String[] def1 = new String[]{"1s", "a"};
            Command Define = new DefineCommand();
            IllegalArgumentException exeption = assertThrows(IllegalArgumentException.class, () -> Define.execute(context, def1));
            assertEquals("Bad first argument.\nFirst argument - variable.", exeption.getMessage());
        }
    }

    @Nested
    @DisplayName("Arithmetic operations Test")
    public class ArithmeticTests{
        @Test
        @DisplayName("Plus operation test")
        public void plusCommandTest(){
            Command Plus = new PlusCommand();
            context.getStack().push(1.2);
            context.getStack().push(2.8);
            Plus.execute(context);
            assertEquals(4.0, context.getStack().get(0));
        }

        @Test
        @DisplayName("Minus operation test")
        public void minusCommandTest(){
            Command Minus = new MinusCommand();
            context.getStack().push(1.2);
            context.getStack().push(2.2);
            Minus.execute(context);
            assertEquals(1.0, context.getStack().get(0));
        }

        @Test
        @DisplayName("Sqrt operation test")
        public void sqrtCommandTest(){
            Command Sqrt = new SqrtCommand();
            context.getStack().push(1.44);
            Sqrt.execute(context);
            assertEquals(1.2, context.getStack().get(0));
        }

        @Test
        @DisplayName("Multiplication operation test")
        public void multiplicationCommandTest(){
            Command Multiplication = new MultiplicationCommand();
            context.getStack().push(2.0);
            context.getStack().push(3.2);
            Multiplication.execute(context);
            assertEquals(6.4, context.getStack().get(0));
        }

        @Test
        @DisplayName("Division operation test")
        public void divisionCommandTest(){
            Command Division = new DivisionCommand();
            context.getStack().push(2.0);
            context.getStack().push(6.4);
            Division.execute(context);
            assertEquals(3.2, context.getStack().get(0));
        }
    }
    /*@Override
    protected void setUp() throws Exception{
        super.setUp();
        cont = new Context();
    }

    public void testPopCommand_emptyStack(){
        Command Pop = new PopCommand();
        Pop.execute(cont);
        assertEquals(cont.getStack().isEmpty(), true);
    }

    public void testPop*/
}
