package se.kumliens.chat.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ExampleTool {

    @Tool(name = "AddingTool", value = "Used for arithmetic addition. The user provides two numbers which will be added together")
    public int add(int a, int b) {
        System.out.println("Adding " + a + " and " + b + " together");
        return a + b;
    }

    @Tool(name = "MultiplyTool", value = "Use for arithmetic multiplication of two numbers")
    public int multiply(int a, int b) {
        return a * b;
    }

    @Tool("This tool is used to get the current date and time in ISO-8601 format")
    public String getCurrentDateAndTime(@P("Dummy variable to avoid NP. Ignore this variable") String ignoreMe) {
        System.out.println("Returning current local date with param " + ignoreMe);
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

}