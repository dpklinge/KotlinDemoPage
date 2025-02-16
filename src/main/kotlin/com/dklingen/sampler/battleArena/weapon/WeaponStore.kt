package com.dklingen.sampler.battleArena.weapon

import com.dklingen.sampler.battleArena.robot.DifficultyClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


public class WeaponStore (
	var inventory: MutableList<RobotWeapon> = mutableListOf(),
	val storeLevel:DifficultyClass,
	var time:LocalDateTime,
	var bossesBeatenLevel: DifficultyClass = DifficultyClass.NONE,
	val random: Random = Random(),
	val bossWeaponChance: Double = 0.05,
	val listSize: Int = 6,
	val weaponFactory: WeaponFactory
)
{
	fun forceUpdate(level: DifficultyClass?): List<RobotWeapon> {
		val storeLevel = level ?: DifficultyClass.NONE
		inventory = mutableListOf()
		for (i in 0..< listSize) {
			inventory.add(weaponFactory.getRandomWeapon(storeLevel, true))
		}
		time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

		return inventory
	}

	fun   checkForNewInventory(achievedLevel: DifficultyClass?): List<RobotWeapon> {
		val storeLevel = achievedLevel ?: DifficultyClass.NONE

		if (LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).compareTo(time.plus(1L, ChronoUnit.MINUTES)) >= 0||storeLevel != achievedLevel) {
			inventory = mutableListOf()
			for (i in 0..< listSize) {
				if(random.nextDouble()<bossWeaponChance && bossesBeatenLevel != DifficultyClass.NONE){
					inventory.add(weaponFactory.generateBossWeapon(bossesBeatenLevel, true))
				}
				inventory.add(weaponFactory.getRandomWeapon(storeLevel, true))
			}
			time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
		}
		return inventory
	}

	fun addBossLevel( enemyLevel: DifficultyClass) {
		if(enemyLevel.difficultyLevel > bossesBeatenLevel.difficultyLevel){
			bossesBeatenLevel = enemyLevel
		}
	}

}
