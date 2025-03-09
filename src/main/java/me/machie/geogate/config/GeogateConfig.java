package me.machie.geogate.config;

import java.util.*;

@SuppressWarnings("unused")
public class GeogateConfig {
    @PreComment("""
        Testing one two
        This is a multiline comment :D
    """)
    @PostComment("This is a postcomment on a primitive")
    public int testPrimitive = 10;

    @PreComment("This is a precomment on an array")
    @PostComment("And this is a postcomment on an array")
    public String[] testArray = {"1", "2", "3"};

    public String partner1 = "I'm stupid";
    public String partner2 = "^ I'm with stupid";

    @PreComment("This is a list!")
    public List<String> testList = new ArrayList<>();
    {
        testList.add("It contains this");
        testList.add("And this");
        testList.add("And also this");
    }
}
