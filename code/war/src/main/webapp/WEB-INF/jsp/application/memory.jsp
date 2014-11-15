<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Random" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Starts a thread that populates the heap and a GC invoker</title>
</head>
<body>

<%
    final Random random = new Random();
    final List<Double> doubles = new ArrayList<Double>();
    Thread thread = new Thread(new Runnable() {
        @SuppressWarnings({"UnnecessaryBoxing", "InfiniteLoopStatement"})
        public void run() {
            int count = 0;
            while (true) {
                count++;
                try {
                    doubles.add(new Double(random.nextDouble()));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (count % 10 == 0) {
                    System.gc();
                }
            }
        }
    });
    thread.start();
%>

</body>
</html>
