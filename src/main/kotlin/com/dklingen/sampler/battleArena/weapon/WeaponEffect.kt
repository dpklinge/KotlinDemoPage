package com.dklingen.sampler.battleArena.weapon

data class WeaponEffect
	(val appellate: String = "",
	val type: EffectType,
	val multiplier: Double = 1.0,
	val criticalChance : Double = 0.1,
	val effectGoldValue: Int,
	val effectStrength: Double)

