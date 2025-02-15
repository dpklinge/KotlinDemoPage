package com.dklingen.sampler.battleArena.weapon

import com.dklingen.sampler.battleArena.controllers.roundToDecimalPlaces
import com.dklingen.sampler.battleArena.robot.DifficultyClass
import com.dklingen.sampler.battleArena.robot.DifficultyClass.*
import com.dklingen.sampler.battleArena.weapon.EffectType.*
import org.springframework.stereotype.Component
import java.lang.Integer.max

import java.util.ArrayList
import java.util.Random

@Component
public class WeaponFactory (val random: Random = Random()){
	val weaponNames = listOf("Torcher", "Slicer", "Basher", "Clobberer", "Tickler", "Stabber", "Frier", "Masher", "Smasher", "Crusher")


	val npcEffects = listOf(ARMOR_PIERCE, CRITICAL_HITS, INNACURATE, VAMPIRISM, RECOIL, CHAOTIC, PARALYSIS, DOUBLE_STRIKE)

	val pcEffects = EffectType.values().asList()

	fun  getWeapon(minEffectNumber: Int, randomExtraEffectMax: Int, minEffectStrength: Double,
			 effectStrengthMax: Double, damageMultiplierMin: Double, randomExtraDamageMultiplier: Double,
			 isPc: Boolean): RobotWeapon {
		val effectNumber = random.nextInt(randomExtraEffectMax + 1) + minEffectNumber
		val effectPercent = random.nextDouble() * effectStrengthMax + minEffectStrength
		val damageMultiplier = damageMultiplierMin + (random.nextDouble() * randomExtraDamageMultiplier)
		val generatedEffects: MutableMap<EffectType, WeaponEffect> = mutableMapOf()
		for (i in 0..<effectNumber) {
			var effect: WeaponEffect
			if (isPc) {
				effect = generateEffect(effectPercent, generatedEffects, pcEffects)
			} else {
				effect = generateEffect(effectPercent, generatedEffects, npcEffects)
			}
			generatedEffects.put(effect.type, effect)
		}
		return RobotWeapon(damageMultiplier = roundToDecimalPlaces(damageMultiplier, 2), weaponEffects = generatedEffects, name = generateName())
	}

	fun getWeakWeapon(isPc: Boolean): RobotWeapon {
		return getWeapon(0, 1, 0.0, 0.2, 0.9, 0.2, isPc)
	}

	fun getMediumWeapon(isPc: Boolean): RobotWeapon {
		return getWeapon(0, 2, 0.2, 0.2, 1.0, .25, isPc)
	}

	fun getStrongWeapon(isPc: Boolean): RobotWeapon {
		return getWeapon(1, 1, 0.3, 0.3, 1.1, .5, isPc)
	}

	fun getLegendaryWeapon(isPc: Boolean): RobotWeapon {
		return getWeapon(1, 2, 0.4, 0.4, 1.25, .75, isPc)
	}

	fun getGodlikeWeapon(isPc: Boolean): RobotWeapon {
		return getWeapon(2, 1, .5, .5, 1.5, 1.0, isPc)
	}

	fun getRandomWeapon( maxLevel: DifficultyClass, isPc: Boolean): RobotWeapon {
		System.out.println("Level of random generation: "+maxLevel.difficultyLevel)
		return when (random.nextInt(max(maxLevel.difficultyLevel,1))) {
			1 -> {
				 getWeakWeapon(isPc)
			}

			2 -> {
				 getMediumWeapon(isPc)
			}

			3 -> {
				 getStrongWeapon(isPc)
			}

			4 -> {
				 getLegendaryWeapon(isPc)
			}

			5 -> {
				 getGodlikeWeapon(isPc)
			}

			else -> {
				getWeakWeapon(isPc)
			}
		}

	}

	fun  generateEffect(effectPercent: Double, alreadyUsedEffects: Map<EffectType, WeaponEffect>,  effects:List<EffectType>): WeaponEffect {
		var type:EffectType
		var notDuplicate: Boolean
		do {
			notDuplicate = true
			type = effects[random.nextInt(effects.size)]
			if (alreadyUsedEffects.containsKey(type)) {
				notDuplicate = false
			}

		} while (!notDuplicate)
		
		val multiplier: Double
		val goldValue: Double
		when (type) {

			CRITICAL_HITS -> {
				multiplier = (1 - (0.10 * effectPercent))
				goldValue = (10000 * effectPercent)
			}

			CHAOTIC -> {
				multiplier = (1 + (0.25 * effectPercent))
				goldValue = (500.0 * effectPercent)
			}

			GOLD_GAINING -> {
				multiplier = (1 - (0.25 * effectPercent))
				goldValue = 250.0
			}

			GOLD_USING -> {
				multiplier = (1.0)
				goldValue = (20000 * effectPercent)
			}

			INNACURATE -> {
				multiplier = (1 + (1.25 * effectPercent))
				goldValue = (100.0)
			}

			PARALYSIS -> {
				multiplier = (1 - (0.5 * effectPercent))
				goldValue = (30000 * effectPercent)
			}

			ARMOR_PIERCE -> {
				multiplier = (1 + (0.2 * effectPercent))
				goldValue = (15000 * effectPercent)
			}

			RECOIL -> {
				multiplier = (1 + (1 * effectPercent))
				goldValue = (2500 * effectPercent)
			}

			VAMPIRISM -> {
				multiplier = (1 - (0.3 * effectPercent))
				goldValue = (20000 * effectPercent)
			}

			DOUBLE_STRIKE -> {
				multiplier = (1.0)
				goldValue = (30000 * effectPercent)
			}
		}

		return WeaponEffect(type = type, effectStrength = effectPercent, multiplier = multiplier, effectGoldValue = goldValue.toInt())
	}
	
	private fun generateName(): String {
		val index = random.nextInt(weaponNames.size)
		return weaponNames[index]
	}

	fun getWeakBossWeapon(isNpc:Boolean): RobotWeapon {
		val weapon: RobotWeapon = getWeapon(1, 0, .25, 0.0, 1.25, 0.0, isNpc)
		bossNameReplace(weapon)
		weapon.value = weapon.value*3
		return weapon
	}
	
	fun bossNameReplace(weapon: RobotWeapon) {
		weapon.description = (weapon.description.replace("Ineffectual", "Boss").replace("Weak", "Boss").replace("Average", "Boss").replace("Strong", "Boss").replace("Powerful", "Boss").replace("Devastating", "Boss"))
		
	}

	fun getMediumBossWeapon(isPc: Boolean): RobotWeapon {
		val weapon: RobotWeapon = getWeapon(1, 1, 0.5, 0.0, 1.35, 0.0, isPc)
		bossNameReplace(weapon)
		weapon.value = weapon.value*3
		return weapon
	}

	fun getStrongBossWeapon(isPc: Boolean): RobotWeapon {
		val weapon: RobotWeapon = getWeapon(2, 0, .6, 0.0, 1.7, 0.0, isPc)
		bossNameReplace(weapon)
		weapon.value = weapon.value*3
		return weapon
	}

	fun getLegendaryBossWeapon(isPc: Boolean): RobotWeapon {
		val weapon: RobotWeapon = getWeapon(2, 1, .8, 0.0 , 2.0, .5, isPc)
		bossNameReplace(weapon)
		weapon.value = weapon.value*3
		return weapon
	}

	fun getGodlikeBossWeapon(isPc: Boolean): RobotWeapon {
		val weapon: RobotWeapon = getWeapon(3, 0, 1.0, 0.0, 2.5, 1.0, isPc)
		bossNameReplace(weapon)
		weapon.value = weapon.value*3
		return weapon
	}

	fun generateBossWeapon(difficultyClass: DifficultyClass, isPc: Boolean): RobotWeapon {
		return when (difficultyClass) {
		 WEAK->  getWeakBossWeapon(isPc)
		AVERAGE->
			 getMediumBossWeapon(isPc)
		STRONG->
			 getStrongBossWeapon(isPc)
		LEGENDARY->
			 getLegendaryBossWeapon(isPc)
		GODLIKE->
			 getGodlikeBossWeapon(isPc)
		else->
			 getWeakBossWeapon(isPc)
		}
		
	}

}
