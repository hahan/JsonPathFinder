import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * Reads Json input and outputs the path to every terminal value in the JSON structure.
 * <p>
 * For example, consider the following JSON object:
 * {
 * "a": 1,
 * "b": true,
 * "c": {
 * "d": 3
 * }
 * }
 * Output of the above object would be:
 * {
 * "a": 1,
 * "b": true,
 * "c.d": 3
 * }
 */
public final class JsonPathFinder {

    private String inputJson;

    public JsonPathFinder(String inputJson) {
        this.inputJson = inputJson;
    }


    public static void main(String... args) throws IOException {
        StringBuilder jsonInput = new StringBuilder();
        Scanner stdin = new Scanner(new BufferedInputStream(System.in));

        //reads the input till <end> marker from stdin
        while (stdin.hasNext()) {
            String nextLine = stdin.nextLine();
            if ("<end>".equals(nextLine))
                break;

            jsonInput.append(nextLine);
        }

        JsonPathFinder jsonPathFinder = new JsonPathFinder(jsonInput.toString());
        jsonPathFinder.print(System.out);
    }

    void print(OutputStream out) throws IOException {
        InputStream in = new ByteArrayInputStream(inputJson.getBytes());

        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        writer.setIndent("    ");

        //this object stack is used to track json elements on a path
        Stack<String> objectStack = new Stack<>();
        writer.beginObject();

        boolean endOfDoc = false;
        while (!endOfDoc) {
            JsonToken token = reader.peek();

            switch (token) {

                case BEGIN_OBJECT:
                    reader.beginObject();
                    break;
                case END_OBJECT:
                    reader.endObject();

                    //pop the element when object ends
                    if (!objectStack.empty())
                        objectStack.pop();
                    break;
                case BEGIN_ARRAY:
                case END_ARRAY:
                    throw new IllegalArgumentException("Array is not supported!");

                case NAME:
                    String name = reader.nextName();
                    objectStack.add(name);
                    break;
                case STRING:
                    //reached the terminal, get the path..
                    beforeValue(writer, objectStack);
                    String s = reader.nextString();
                    writer.value(s);
                    //.. and remove the last element from the path
                    objectStack.pop();
                    break;
                case NUMBER:
                    //reached the terminal, get the path..
                    beforeValue(writer, objectStack);
                    String n = reader.nextString();
                    writer.value(new BigDecimal(n));
                    //.. and remove the last element from the path
                    objectStack.pop();
                    break;
                case BOOLEAN:
                    //reached the terminal, get the path..
                    beforeValue(writer, objectStack);
                    boolean b = reader.nextBoolean();
                    writer.value(b);
                    //.. and remove the last element from the path
                    objectStack.pop();
                    break;
                case NULL:
                    //reached the terminal, get the path..
                    beforeValue(writer, objectStack);
                    reader.nextNull();
                    writer.nullValue();
                    //.. and remove the last element from the path
                    objectStack.pop();
                    break;
                case END_DOCUMENT:
                    writer.endObject();
                    endOfDoc = true;
                    break;
            }
        }

        writer.close();
        reader.close();
    }

    /**
     * Handles when the terminal value is found. It builds the path and add to the output.
     */
    private void beforeValue(JsonWriter writer, Stack<String> objectStack) throws IOException {
        String path = buildPath(objectStack);
        writer.name(path);
    }

    /**
     * Builds the path by reading the elements in objectStack and add "." between them.
     * <p>
     * E.g. in case of,
     * {
     * "a":{"c":3}
     * }
     * When terminal "3" is reached, the stack will have {c, a} where c is at top of the stack. The method will build the path as "a.c".
     */
    private String buildPath(List<String> objectStack) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String name : objectStack) {
            if (stringBuffer.length() != 0)
                stringBuffer.append(".");

            stringBuffer.append(name);
        }
        return stringBuffer.toString();
    }
}

