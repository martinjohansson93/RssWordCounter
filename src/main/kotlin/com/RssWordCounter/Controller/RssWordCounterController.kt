package com.RssWordCounter.Controller

import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileNotFoundException
import java.net.MalformedURLException

import java.net.URL


@RestController
@RequestMapping("/")
class RssWordCounterController{

    @PostMapping("/rssfeed/onehundredmostcommonwords")
    fun oneHundredMostCommonWordsInRssFeed(@RequestBody rssRequestString: String): ResponseEntity<Any> {
        // Get the link to the Rss file from the request. If it fails return 400 - Bad request.
        var rssRequestJson = JSONObject(rssRequestString)
        if (!rssRequestJson.has("rssLink")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Missing key rssLink."))
        }

        // Verify the URL by creating an URL object. If it fails return 400 - Bad request.
        var urlObject: URL
        try {
            urlObject = URL(rssRequestJson.get("rssLink").toString())
        }   catch(e: MalformedURLException) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Provided value for key rssLink is not an URL."))
        }

        // Read the URL object. If it fails return 400 - Bad request.
        var responseXmlString: String
        try {
            responseXmlString = urlObject.readText()
        } catch (e: FileNotFoundException) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Failed to retrieve information from rss link."))
        }

        // Check that the response from the Url is a Rss document. If it fails return 400 - Bad request.
        if (!responseXmlString.contains("<rss version=") || !responseXmlString.contains("</rss>")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Information retrieved not in correct rss format."))
        }

        val allWords = parseXmlToListOfAllWords(responseXmlString)
        val occurrencesOfWords = countOccurrenceOfWords(allWords)

        val response = occurrencesOfWords.toList()
                                         .sortedByDescending { (_, value) -> value }
                                         .take(100).toMap()
        return ResponseEntity.ok().body(mapOf("data" to response))

    }

    fun countOccurrenceOfWords(allWords: ArrayList<String>): Map<String, Int> {
        var count = mutableMapOf<String, Int>()
        for (word in allWords)  {
            if (word in count)  {
                count[word] = count[word]!!.plus(1)
            } else {
                count[word] = 1
            }
        }
        return count
    }

    fun parseXmlToListOfAllWords(xmlString: String): ArrayList<String> {
        // Check if it contains any items, return if not
        if (!xmlString.contains("<item>") && !xmlString.contains("</item>")) return arrayListOf()

        // Find all items in the feed
        var itemsXmlFormat = xmlString.split("</item>")
        var allProcessedWords: ArrayList<String> = ArrayList()

        for (itemXmlFormat in itemsXmlFormat) {
            val splitItemXmlFormat = itemXmlFormat.split("<item>")

            // If it is the first item it will be on index 1 due to header information, else index 0.
            var index = if (splitItemXmlFormat.size > 1) 1 else 0
            val item = splitItemXmlFormat[index]

            // Find all words in the title
            var titleList = item.split("<title>")
            var potentialWords: ArrayList<String> = ArrayList()
            if (titleList.size > 1) {
                val title = titleList[1].split("</title>")[0]
                potentialWords.addAll(title.split(" "))
            }

            // Find all words in the description
            var descriptionList = item.split("<description>")
            if (descriptionList.size > 1)   {
                val description = descriptionList[1].split("</description>")[0]
                potentialWords.addAll(description.replace("&nbsp;", " ").split(" "))
            }

            // Process all words found in the title and the description, if correct add them to list of all words.
            for (word in potentialWords) {
                // Start and end of xml field that may contain "<![CDATA[" and "]]>". Replace this.
                // Then first trim newlines and whitespaces. After that special characters.
                val preparedWord = word.replace("<![CDATA[", "")
                                       .replace("]]>", "")
                                       .trim()
                                       .trim('.', ',', '\'', ':', ';', '?', '!', '"', '`', '´', '”', '’', '“')
                                       .toLowerCase()
                                       .replace("&#8217;", "'")
                if (preparedWord.isNotEmpty()) {
                    allProcessedWords.add(preparedWord)
                }
            }

        }
        return allProcessedWords
    }
}

