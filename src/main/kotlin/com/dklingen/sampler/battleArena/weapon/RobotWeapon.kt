package com.dklingen.sampler.battleArena.weapon

import com.dklingen.sampler.battleArena.controllers.roundToDecimalPlaces
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.HashMap
import kotlin.math.round

data class RobotWeapon (
	val weaponEffects: MutableMap<EffectType, WeaponEffect> = mutableMapOf(),
	var damageMultiplier: Double,
	var value: Int = 100,
	val name: String,
	var description: String = ""
){
	init {
	    for(entry: MutableMap.MutableEntry<EffectType, WeaponEffect> in weaponEffects.entries){
			processEffectValueChange(entry.value)
		}
		description = generateDescription()
	}
	private fun processEffectValueChange(effect: WeaponEffect ){
		damageMultiplier += (effect.multiplier - 1)

		damageMultiplier = roundToDecimalPlaces(damageMultiplier, 2)
		value+=effect.effectGoldValue
		weaponEffects.put(effect.type, effect)
	}

	private fun  generateDescription(): String {
		var description = ""
		for (effect: WeaponEffect  in weaponEffects.values) {
			description += effect.type.descriptor + " "
		}
		description += strengthAppelate(damageMultiplier) + " "
		description += name
		return description

	}

	private fun strengthAppelate(damageMultiplier: Double): String {
		return if (damageMultiplier < .5) {
			"Ineffectual"
		} else if (damageMultiplier < .9) {
			"Weak"
		} else if (damageMultiplier < 1.1) {
			"Average"
		} else if (damageMultiplier < 1.5) {
			"Strong"
		} else if (damageMultiplier < 2) {
			"Powerful"
		} else {
			"Devastating"
		}
	}
}
