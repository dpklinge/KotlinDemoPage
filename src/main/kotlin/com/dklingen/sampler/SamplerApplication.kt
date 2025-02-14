package com.dklingen.sampler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SamplerApplication

fun main(args: Array<String>) {
	runApplication<SamplerApplication>(*args)
}
