# Rss reader


### Run the server
Import the project as a gradle project into jetbrains intellij. Congifure an "Application" with the main method "com.RssWordCounter.RssWordCounterApplicationKt" and the classpath of module "com.RssWordCounter.main"


### Rest API
This programs exposes the endpoint "rssfeed/onehundredmostcommonwords", it is used by sending a JSON request containg the key rssLink and provide an URL to the rss xml feed.

For example sending a post request with the following json object.


```JSON
{
	"rssLink": "http://www1.cbn.com/app_feeds/rss/news/rss.php?section=world"
}
```

