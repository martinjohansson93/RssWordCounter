package com.RssWordCounter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RssWordCounterApplication

fun main(args: Array<String>) {
	runApplication<RssWordCounterApplication>(*args)
}
