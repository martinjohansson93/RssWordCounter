package com.RssWordCounter

import com.RssWordCounter.Controller.RssWordCounterController

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import java.io.File
import java.nio.file.Paths

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class RssWordCounterControllerTest {

	@Autowired
	private val mvc: MockMvc? = null

	@Test
	@Throws(Exception::class)
	fun oneHundredMostCommonWordsInRssFeedCorrectRequestExpecting200() {
		// Optimally should the outgoing request be caught and the response mocked predictable value.
		// Which would make it possible to check the function of the whole endpoint as well.
		// Content on this changes so can only test the status code.
		val requestBody = "{'rssLink': 'http://www1.cbn.com/app_feeds/rss/news/rss.php?section=world'}"
		mvc!!.perform(MockMvcRequestBuilders.post("/rssfeed/onehundredmostcommonwords").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isOk)
	}

	@Test
	@Throws(Exception::class)
	fun oneHundredMostCommonWordsInRssFeedEmptyPostRequestExpecting400() {
		var requestBody = """{}"""
		this.mvc!!.perform(MockMvcRequestBuilders.post("/rssfeed/onehundredmostcommonwords").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isBadRequest).andExpect(MockMvcResultMatchers.content().json("{'error': 'Missing key rssLink.'}"))
	}

	@Test
	@Throws(Exception::class)
	fun oneHundredMostCommonWordsInRssFeedNoRssLinkKeyExpecting400() {
		var requestBody = """{'notRssLink': 'http://www1.cbn.com/app_feeds/rss/news/rss.php?section=world', "error": "Also not rss link"}"""
		this.mvc!!.perform(MockMvcRequestBuilders.post("/rssfeed/onehundredmostcommonwords").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isBadRequest).andExpect(MockMvcResultMatchers.content().json("{'error': 'Missing key rssLink.'}"))
	}

	@Test
	@Throws(Exception::class)
	fun oneHundredMostCommonWordsInRssFeedBadRssLinkExpecting400() {
		var requestBody = """{'rssLink': 'http://www1.cbn.com/app_feeds/rss/news/rss.php?section=worldtestTestWrong'}"""
		this.mvc!!.perform(MockMvcRequestBuilders.post("/rssfeed/onehundredmostcommonwords").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isBadRequest).andExpect(MockMvcResultMatchers.content().json("{'error': 'Information retrieved not in correct rss format.'}"))
	}

	@Test
	@Throws(Exception::class)
	fun oneHundredMostCommonWordsInRssFeedEmptyRssLinkExpecting400() {
		var requestBody = """{'rssLink': ''}"""
		this.mvc!!.perform(MockMvcRequestBuilders.post("/rssfeed/onehundredmostcommonwords").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isBadRequest).andExpect(MockMvcResultMatchers.content().json("{'error': 'Provided value for key rssLink is not an URL.'}"))
	}

	// Test of function parseXmlToListOfAllWords
	@Test
	fun parseXmlToListOfAllWordsRssCorrectXml() {
		val xmlString = File(Paths.get("").toAbsolutePath().toString() + "/src/test/kotlin/com/RssWordCounter/resources/CorrectXml.xml").readText()
		val rssReader = RssWordCounterController()
		val result = rssReader.parseXmlToListOfAllWords(xmlString)
		val expectedResult: ArrayList<String> = arrayListOf("hello", "world", "description", "hello", "this", "is", "a", "message")
		Assert.assertEquals(expectedResult, result)
	}

	@Test
	fun parseXmlToListOfAllWordsCorrectRssXmlWithCdataTag() {
		val xmlString = File(Paths.get("").toAbsolutePath().toString() + "/src/test/kotlin/com/RssWordCounter/resources/CorrectXmlWithCdataTag.xml").readText()
		val rssReader = RssWordCounterController()
		val result = rssReader.parseXmlToListOfAllWords(xmlString)
		val expectedResult: ArrayList<String> = arrayListOf("hello", "world", "description", "hello", "this", "is", "a", "message")
		Assert.assertEquals(expectedResult, result)
	}

	@Test
	fun parseXmlToListOfAllWordsEmptyItems() {
		val xmlString = File(Paths.get("").toAbsolutePath().toString() + "/src/test/kotlin/com/RssWordCounter/resources/EmptyItems.xml").readText()
		val rssReader = RssWordCounterController()
		val result = rssReader.parseXmlToListOfAllWords(xmlString)
		val expectedResult: ArrayList<String> = arrayListOf()
		Assert.assertEquals(expectedResult, result)
	}

	@Test
	fun parseXmlToListOfAllWordsEmptyItemsWithCdataTag() {

		val xmlString = File(Paths.get("").toAbsolutePath().toString() + "/src/test/kotlin/com/RssWordCounter/resources/EmptyItemsWithCdataTag.xml").readText()
		val rssReader = RssWordCounterController()
		val result = rssReader.parseXmlToListOfAllWords(xmlString)
		val expectedResult: ArrayList<String> = arrayListOf()
		Assert.assertEquals(expectedResult, result)
	}

	@Test
	fun parseXmlToListOfAllWordsNoItems() {
		val xmlString = File(Paths.get("").toAbsolutePath().toString() + "/src/test/kotlin/com/RssWordCounter/resources/NoItemsXml.pom").readText()
		val rssReader = RssWordCounterController()
		val result = rssReader.parseXmlToListOfAllWords(xmlString)
		val expectedResult: ArrayList<String> = arrayListOf()
		Assert.assertEquals(expectedResult, result)
	}

	// Test of function countOccurrenceOfWords

	@Test
	fun countOccurrenceOfWordsCorrectInput() {
		val input: ArrayList<String> = arrayListOf("hello", "world", "description", "hello", "this", "is", "a", "message")
		val rssReader = RssWordCounterController()
		val result = rssReader.countOccurrenceOfWords(input)
		val expectedResult = mapOf("hello" to 2, "world" to 1, "description" to 1, "this" to 1, "is" to 1, "a" to 1, "message" to 1)
		Assert.assertEquals(expectedResult, result)
	}

	@Test
	fun countOccurrenceOfWordsEmptyInput() {
		val input: ArrayList<String> = arrayListOf()
		val rssReader = RssWordCounterController()
		val result = rssReader.countOccurrenceOfWords(input)
		val expectedResult = mapOf<String, Int>()
		Assert.assertEquals(expectedResult, result)
	}
}
