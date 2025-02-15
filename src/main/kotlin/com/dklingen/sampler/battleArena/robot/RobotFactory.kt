package com.dklingen.sampler.battleArena.robot

import com.dklingen.sampler.battleArena.Battle
import com.dklingen.sampler.battleArena.getMaxSpeed
import com.dklingen.sampler.battleArena.weapon.RobotWeapon
import com.dklingen.sampler.battleArena.weapon.WeaponFactory
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.Random

@Component
class RobotFactory @Autowired constructor(private val factory: WeaponFactory) {
	private val random = Random()
	private val goldenRobotChance = 0.04
	private val bossChance = 0.02

	fun generateRobotName(robot: BattleRobot): String {
		var name = RandomStringUtils.randomAlphanumeric(3, random.nextInt(5) + 3) + " the"

		when {
			robot.maxHealth > 2000 -> name += " Robust"
			robot.maxHealth > 1000 -> name += " Sturdy"
			robot.maxHealth < 200 -> name += " Frail"
		}

		when {
			robot.armor > 50 -> name += " Tanky"
			robot.armor < 20 -> name += " Fragile"
		}

		when {
			robot.speed > 50 -> name += " Swift"
			robot.speed < 20 -> name += " Slow"
		}

		when {
			robot.strength > 50 -> name += " Crusher"
			robot.strength < 20 -> name += " Weak"
		}

		return "$name BattleBot"
	}

	fun generateEnemy(
		minSpeed: Int, maxRandomAddSpeed: Int,
		minStrength: Int, maxRandomAddStrength: Int,
		minArmor: Int, maxRandomAddArmor: Int,
		minHealth: Int, maxRandomHealth: Int,
		valueMultiplier: Double, nullableOverrideName: String?, clazz: DifficultyClass
	): BattleRobot {


		val speed = if (maxRandomAddSpeed > 0) random.nextInt(maxRandomAddSpeed) + minSpeed else minSpeed
		val strength = if (maxRandomAddStrength > 0) random.nextInt(maxRandomAddStrength) + minStrength else minStrength
		val armor = if (maxRandomAddArmor > 0) random.nextInt(maxRandomAddArmor) + minArmor else minArmor
		val maxHealth = if (maxRandomHealth > 0) random.nextInt(maxRandomHealth) + minHealth else minHealth



		val weapon: RobotWeapon = when (clazz) {
			DifficultyClass.WEAK -> factory.getWeakWeapon(false)
			DifficultyClass.AVERAGE -> factory.getMediumWeapon(false)
			DifficultyClass.STRONG -> factory.getStrongWeapon(false)
			DifficultyClass.LEGENDARY -> factory.getLegendaryWeapon(false)
			DifficultyClass.GODLIKE -> factory.getGodlikeWeapon(false)
			else-> {
				factory.getWeakWeapon(false)
			}
		}
		val robot = BattleRobot(weapon = weapon)
		robot.apply {
			this.armor = armor
			this.currentHealth = maxHealth
			this.maxHealth = maxHealth
			this.speed = speed
			this.strength = strength
			this.name = nullableOverrideName ?: generateRobotName(this)
		}

		robot.updateValue()
		robot.value = (robot.value * valueMultiplier).toInt()

		val upgradeChance = random.nextDouble()
		when {
			upgradeChance < bossChance -> {
				robot.name = "Boss ${robot.name}"
				robot.apply {
					this.armor = (armor * 2.5).toInt()
					this.currentHealth = (currentHealth * 2.5).toInt()
					this.maxHealth = (maxHealth * 2.5).toInt()
					this.speed = (speed * 2.5).toInt()
					this.strength = (strength * 2.5).toInt()
					this.weapon = factory.generateBossWeapon(clazz, false)
				}
				robot.updateValue()
				robot.value = robot.value * 5 + 1000
			}
			upgradeChance < goldenRobotChance -> {
				robot.name = "Golden ${robot.name}"
				robot.apply {
					this.armor = (armor * 1.3).toInt()
					this.currentHealth = (currentHealth * 1.3).toInt()
					this.maxHealth = (maxHealth * 1.3).toInt()
					this.speed = (speed * 1.3).toInt()
					this.strength = (strength * 1.3).toInt()
					this.value = value * 2 + 1000
				}
			}
		}

		return robot
	}

	fun generateEasyEnemy() = generateEnemy(1, 15, 1, 15, 1, 15, 50, 200, 0.5, null, DifficultyClass.WEAK)
	fun generateMediumEnemy() = generateEnemy(10, 30, 10, 30, 10, 30, 250, 500, 0.75, null, DifficultyClass.AVERAGE)
	fun generateHardEnemy() = generateEnemy(30, 20, 30, 50, 30, 50, 500, 1000, 1.0, null, DifficultyClass.STRONG)
	fun generateLegendaryEnemy() = generateEnemy(50, 50, 50, 100, 50, 100, 1000, 2000, 1.25, null, DifficultyClass.LEGENDARY)
	fun generateGodlikeEnemy() = generateEnemy(
		getMaxSpeed(), 0, 250, 0, 250, 0, 10000, 0, 1.5, "God Incarnate", DifficultyClass.GODLIKE
	)
}
