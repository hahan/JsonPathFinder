# JsonPathFinder

Run the command as
  java -jar out/artifacts/jsonparser_jar/jsonparser.jar

Input the json in stdin and then following the input, add string <end> on a newline.

The output of the program will follow the input.

E.g.

```

$ java -jar out/artifacts/jsonparser_jar/jsonparser.jar
  {
  "a":{"c":1}
  }
  (newline, press enter)
  <end>
  {
      "a.c": 1
  }%

```

