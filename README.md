Tests that Lucene 10 produces
```
"[abc]+" -> "abc"
"[ABC]+" -> "ABC"
"[a-c]+" -> "abc"
"[A-C]+" -> "ABC"
```

While ES 8.16 does

```
"[abc]+" -> "abc", "ABC"
"[ABC]+" -> "abc", "ABC"
"[a-c]+" -> "abc"
"[A-C]+" -> "ABC"
```


mvn compile && mvn exec:java -Dexec.mainClass="com.example.LuceneRegexExample"

