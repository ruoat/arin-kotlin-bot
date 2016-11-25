package fi.iki.atr

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ArinKotlinBotApplication

fun main(args: Array<String>) {
    SpringApplication.run(ArinKotlinBotApplication::class.java, *args)
}
