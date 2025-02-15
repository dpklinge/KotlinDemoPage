package com.dklingen.sampler.battleArena

import com.dklingen.sampler.battleArena.robot.BattleRobot
import com.dklingen.sampler.battleArena.weapon.EffectType
import com.dklingen.sampler.battleArena.weapon.RobotWeapon
import com.dklingen.sampler.battleArena.weapon.WeaponEffect
import org.springframework.stereotype.Component

import java.util.Random
const val turnspeed = 100
fun getMaxSpeed() = turnspeed

@Component
class Battle(
	val random: Random = Random()
) {

	fun doBattle(playerRobot: BattleRobot, enemyRobot: BattleRobot): BattleResults {
		val results: BattleResults = BattleResults(attacker = playerRobot, defender = enemyRobot)
		results.addToLog("Battle between ${playerRobot.name} and ${enemyRobot.name}")
		var playerSpeedCount = 0
		var enemySpeedCount = 0
		System.out.println("Entering combat loop")
		var counter = 0
		while (playerRobot.currentHealth > 0 && enemyRobot.currentHealth > 0 && counter < 10000) {
			System.out.println("Combat loop")
			playerSpeedCount += playerRobot.speed
			enemySpeedCount += enemyRobot.speed
			while ((playerSpeedCount / turnspeed) > 0 && (enemySpeedCount / turnspeed) > 0
					&& playerRobot.currentHealth > 0 && enemyRobot.currentHealth > 0) {
				if (playerSpeedCount / turnspeed > 0 && playerSpeedCount>=enemySpeedCount) {
					attemptStrike(playerRobot, enemyRobot, results)
					playerSpeedCount -= turnspeed
				}
				if (enemySpeedCount / turnspeed > 0 && enemyRobot.currentHealth > 0 && enemySpeedCount>playerSpeedCount ) {
					attemptStrike(enemyRobot, playerRobot, results)
					enemySpeedCount -= turnspeed
				}
				counter++
			}
		}
		if (counter > 10000) {
			results.addToLog("Battle resulted in a draw.")
		}
		results.isWin = playerRobot.currentHealth > 0
		return results
	}

	fun attemptStrike( attacker: BattleRobot, defender: BattleRobot, results: BattleResults) {
		if (attacker.isParalyzed) {
			results.addToLog(attacker.name + " was paralyzed by " + defender.name + "'s "
					+ defender.weapon.description + " and could not attack!")
			attacker.isParalyzed=false
		} else if (attacker.weapon.weaponEffects.containsKey(EffectType.INNACURATE)) {
			val hitRoll = random.nextDouble()
			if (hitRoll < ((attacker.weapon.weaponEffects.get(EffectType.INNACURATE)?.effectStrength?: 0.0) / 2)) {
				results.addToLog(attacker.name + " missed!")
			} else {
				strike(attacker, defender, results)
			}
		} else {
			strike(attacker, defender, results)
		}

	}

	fun strike(attacker: BattleRobot, defender: BattleRobot, results: BattleResults) {

		if (attacker.weapon.weaponEffects.containsKey(EffectType.DOUBLE_STRIKE)) {
			val hitRoll = random.nextDouble()
			if (hitRoll < (attacker.weapon.weaponEffects.get(EffectType.DOUBLE_STRIKE)?.effectStrength ?: 0.0) /2.0 ) {
				doDamage(attacker, defender, results)
				results.addToLog(attacker.name + "'s double-strike weapon hits again!")
			}
		}
		doDamage(attacker, defender, results)
		if (attacker.weapon.weaponEffects.containsKey(EffectType.CHAOTIC)) {
			val hitRoll = random.nextDouble()
			if (hitRoll < .02) {
				results.addToLog(
						attacker.name + "'s chaotic weapon chain strikes, doubling the number of attacks!")
				strike(attacker, defender, results)
			}
		}

	}

	fun applyParalysis(defender: BattleRobot, attacker: BattleRobot, weaponEffect: WeaponEffect,
			results: BattleResults) {

		if (random.nextDouble() < (weaponEffect.effectStrength * 0.5)) {
			results.addToLog(
					attacker.name + "'s weapon paralyzes " + defender.name + "! They cannot attack!")
			defender.isParalyzed = true
		}
	}

	fun doDamage(attacker: BattleRobot, defender: BattleRobot, results: BattleResults) {
		val weapon: RobotWeapon = attacker.weapon

		var damage: Double = ((random.nextDouble() + 0.5) * attacker.strength.toDouble() * weapon.damageMultiplier)

		if (attacker.weapon.weaponEffects.containsKey(EffectType.GOLD_USING)) {
			damage = applyGoldUsing(damage, attacker.weapon.weaponEffects[EffectType.GOLD_USING]!!,
					attacker, defender, results)
		}
		if (attacker.weapon.weaponEffects.containsKey(EffectType.CRITICAL_HITS)) {
			damage = applyCriticals(damage, attacker.weapon.weaponEffects[EffectType.CRITICAL_HITS]!!,
					defender, results)
		}
		if (attacker.weapon.weaponEffects.containsKey(EffectType.ARMOR_PIERCE)) {
			damage = applyArmorPierce(damage, attacker.weapon.weaponEffects[EffectType.ARMOR_PIERCE]!!,
					defender, results)
		}
		if (attacker.weapon.weaponEffects.containsKey(EffectType.CHAOTIC)) {
			damage = applyChaoticEffects(attacker, damage,
					attacker.weapon.weaponEffects[EffectType.CHAOTIC]!!, defender, results)
		}
		if (attacker.weapon.weaponEffects.containsKey(EffectType.PARALYSIS)) {
			applyParalysis(defender, attacker, attacker.weapon.weaponEffects[EffectType.PARALYSIS]!!,
					results)
		}
		damage = applyArmorDamageReduction(defender, attacker, damage)

		defender.currentHealth = defender.currentHealth - damage.toInt()
		results.addToLog(defender.name + " was struck for " + damage.toInt() + " damage!")

		applyRecoil(damage, attacker, defender, results)
		if (attacker.weapon.weaponEffects.containsKey(EffectType.GOLD_GAINING)) {
			applyGoldLeech(attacker, damage, attacker.weapon.weaponEffects[EffectType.GOLD_GAINING]!!,
					defender, results)
		}
		applyVampirism(damage, attacker, defender, results)

	}

	fun applyArmorDamageReduction(defender: BattleRobot, attacker: BattleRobot, damage: Double ): Double {
		var newDamage = damage
		if (damage <= defender.armor) {
			newDamage = 1.0
		} else {
			newDamage -= defender.armor
		}
		return newDamage
	}

	fun applyChaoticEffects(attacker: BattleRobot, damage: Double , weaponEffect: WeaponEffect, defender: BattleRobot,
			results: BattleResults): Double {
		val rand = random.nextDouble()
		var newDamage:Double = damage
		if (rand < 0.10) {
			results.addToLog("Chaotic weapon dealt reduced damage!")
			newDamage -= newDamage * weaponEffect.effectStrength
		}else if (rand < 0.20) {
			results.addToLog("Chaotic weapon had no special effect.")
		}else if (rand < 0.25) {
			results.addToLog("Chaotic weapon dealt increased damage!")
			newDamage += newDamage * weaponEffect.effectStrength
		} else if (rand < 0.35) {
			results.addToLog("Chaotic weapon attempted to paralyze!")
			applyParalysis(defender, attacker, weaponEffect, results)
		} else if (rand < 0.45) {
			results.addToLog("Chaotic weapon converts money to damage!")
			newDamage = applyGoldUsing(newDamage, weaponEffect, attacker, defender, results)
		} else if (rand < 0.55) {
			results.addToLog("Chaotic weapon punctures the enemy armor!")
			newDamage = applyArmorPierce(newDamage, weaponEffect, defender, results)
		} else if (rand < 0.65) {
			results.addToLog("Chaotic weapon vampirically leaches the enemy!")
			applyVampirism(newDamage, attacker, defender, results)
		} else if (rand < 0.75) {
			results.addToLog("Chaotic weapon deals a critical hit!")
			newDamage = applyCriticals(newDamage, weaponEffect, defender, results)
		} else if (rand < 0.85) {
			if (attacker == results.attacker) {
				results.addToLog("Chaotic weapon leaches funds!")
				applyGoldLeech(attacker, newDamage, weaponEffect, defender, results)
			}
		} else if (rand < 0.998) {
			results.addToLog("Chaotic damage fluctuates wildly!")
			newDamage = (random.nextDouble() * 3 * newDamage)
		} else {
			results.addToLog("******JACKPOT!******")
			results.addToLog("Gold overflows from your chaotic weapon!")
			results.updateWinnings(attacker, 10000)
		}
		return newDamage
	}

	fun applyVampirism(damage: Double , attacker: BattleRobot, defender: BattleRobot, results: BattleResults) {
		if (attacker.weapon.weaponEffects.containsKey(EffectType.VAMPIRISM)) {
			var healing = (damage * (attacker.weapon.weaponEffects[EffectType.VAMPIRISM]?.effectStrength?:0.0))
			if (healing < 1) {
				healing = 1.0
			}
			val newHealth = (attacker.currentHealth + healing)
			if (newHealth > attacker.maxHealth) {
				attacker.currentHealth = attacker.maxHealth
				results.addToLog(attacker.name + " vampirically healed to full health! ")
			} else {
				attacker.currentHealth = attacker.currentHealth + healing.toInt()
				results.addToLog(attacker.name + " vampirically healed " + healing.toInt() + "! ")
			}
		}

	}

	fun applyGoldLeech(attacker: BattleRobot, damage: Double , weaponEffect: WeaponEffect, defender: BattleRobot,
			results: BattleResults) {
		if (attacker == results.attacker) {
			System.out.println("Potential winnings before gold leech: " + results.getWinnings(attacker))
			val moneyGain = (damage * weaponEffect.effectStrength)
			System.out.println("Money leeched by gold leech: $moneyGain")
			if (moneyGain > 0) {
				results.addToLog("Thrifty weapon extracts " + moneyGain + "g from foe.")
				results.updateWinnings(attacker,  moneyGain.toInt())
				System.out.println("Potential winnings after gold leech: " + results.getWinnings(attacker))
			}
		}

	}

	fun applyRecoil(damage: Double , attacker: BattleRobot, defender: BattleRobot, results: BattleResults) {
		if (attacker.weapon.weaponEffects.containsKey(EffectType.RECOIL)) {
			var recoil = ((attacker.weapon.weaponEffects[EffectType.RECOIL]?.effectStrength?:0.0) * damage / 2)
			if (recoil < 1) {
				recoil = 1.0
			}
			if (recoil > attacker.currentHealth) {
				results.addToLog(
						attacker.name + " suffers " + (attacker.currentHealth - 1) + " damage in recoil.")
				attacker.currentHealth = 1
			} else {
				results.addToLog(attacker.name + " suffers " + recoil.toInt() + " damage in recoil.")
				attacker.currentHealth = attacker.currentHealth - recoil.toInt()
			}
		}

	}

	fun applyArmorPierce(damage: Double , weaponEffect: WeaponEffect, defender: BattleRobot, results: BattleResults): Double {
		var newDamage = damage
		var pierce = weaponEffect.effectStrength * 0.75 * defender.armor
		results.addToLog("${pierce.toInt()} armor negated!")
		newDamage += pierce
		return newDamage

	}

	fun applyCriticals(damage: Double , effect: WeaponEffect, defender: BattleRobot, results: BattleResults): Double {
		var newDamage = damage
		if (random.nextDouble() < (effect.effectStrength)) {
			results.addToLog("Critical hit! ")
			newDamage *= 1.5
		}
		return newDamage
	}

	fun applyGoldUsing(damage: Double , effect: WeaponEffect , attacker: BattleRobot, defender: BattleRobot, results: BattleResults): Double {
		var newDamage = damage
		val percentValueDamage = effect.effectStrength * 0.05
		val bonusDamage = defender.value * percentValueDamage
		newDamage += bonusDamage
		results.updateWinnings(attacker, -bonusDamage.toInt())
		results.addToLog("Expensive weapon converts $bonusDamage potential winnings to boost damage!")
		return newDamage

	}

}
