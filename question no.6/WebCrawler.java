import java.util.*;
import java.util.concurrent.*;

// Interface for HTML Parser that retrieves URLs from a given page
interface HtmlParser {
    List<String> getUrls(String url);
}

// WebCrawler class to perform multi-threaded web crawling
public class WebCrawler {
    public static void main(String[] args) {
        // Mock implementation of HtmlParser to simulate crawling behavior
        HtmlParser parser = new HtmlParser() {
            private Map<String, List<String>> urlMap = new HashMap<>();
            {
                // Define URL relationships (a mock web structure)
                urlMap.put("http://news.yahoo.com", Arrays.asList(
                    "http://news.yahoo.com/news",
                    "http://news.yahoo.com/us"
                ));
                urlMap.put("http://news.yahoo.com/news", Arrays.asList(
                    "http://news.yahoo.com/news/topics/"
                ));
                urlMap.put("http://news.yahoo.com/news/topics/", Collections.emptyList());
                urlMap.put("http://news.google.com", Collections.emptyList());
                urlMap.put("http://news.yahoo.com/us", Collections.emptyList());
            }

            @Override
            public List<String> getUrls(String url) {
                return urlMap.getOrDefault(url, Collections.emptyList());
            }
        };

        // Create a WebCrawler instance
        WebCrawler crawler = new WebCrawler();
        
        // Test case 1: Start crawling from Yahoo's homepage
        System.out.println("Test Case 1 - Start with yahoo.com:");
        List<String> result1 = crawler.crawl("http://news.yahoo.com", parser);
        System.out.println("Crawled URLs: " + result1);

        // Test case 2: Start crawling from Google News
        System.out.println("\nTest Case 2 - Start with google.com:");
        List<String> result2 = crawler.crawl("http://news.google.com", parser);
        System.out.println("Crawled URLs: " + result2);
    }

    /**
     * Multi-threaded web crawler using thread pool.
     * @param startUrl The starting URL.
     * @param htmlParser The parser to extract URLs.
     * @return A list of URLs that belong to the same domain.
     */
    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        // Extract hostname from the given start URL
        String hostName = getHostName(startUrl);

        List<String> res = new ArrayList<>();
        Set<String> visited = new HashSet<>(); // To track visited URLs
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        Deque<Future<?>> tasks = new ArrayDeque<>();

        queue.offer(startUrl); // Start with the initial URL

        // Create a thread pool of 4 threads for concurrent URL fetching
        ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            // Use daemon threads so the program can exit automatically
            t.setDaemon(true);
            return t;
        });

        // Process the URLs in the queue
        while (true) {
            String url = queue.poll();
            if (url != null) {
                // Ensure that the URL belongs to the same domain and is not visited
                if (getHostName(url).equals(hostName) && !visited.contains(url)) {
                    res.add(url);
                    visited.add(url);

                    // Submit a new task to fetch URLs in a separate thread
                    tasks.add(executor.submit(() -> {
                        List<String> newUrls = htmlParser.getUrls(url);
                        for (String newUrl : newUrls) {
                            queue.offer(newUrl);
                        }
                    }));
                }
            } else {
                // If the queue is empty, wait for pending tasks to finish
                if (!tasks.isEmpty()) {
                    Future<?> nextTask = tasks.poll();
                    try {
                        nextTask.get(); // Wait for task completion
                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // No more tasks, exit the loop
                    break;
                }
            }
        }

        return res; // Return the list of crawled URLs
    }
    
    /**
     * Extracts the hostname from a given URL.
     * @param url The full URL.
     * @return The hostname of the URL.
     */
    private String getHostName(String url) {
        url = url.substring(7); // Remove "http://"
        String[] parts = url.split("/");
        return parts[0]; // Extract the domain name
    }
}
