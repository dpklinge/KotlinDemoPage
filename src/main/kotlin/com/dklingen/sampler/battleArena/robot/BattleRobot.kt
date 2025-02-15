package com.dklingen.sampler.battleArena.robot

import com.dklingen.sampler.battleArena.weapon.RobotWeapon
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

 class BattleRobot (
	var currentHealth: Int = 100,
	var maxHealth: Int = 100,
	var speed: Int = 5,
	var strength: Int = 5,
	var armor: Int = 5,
	var value: Int = 0,
	var isParalyzed: Boolean = false,
	var name: String ="Generic Robot",
	var weapon: RobotWeapon,
//	var webDescription: String=  ""
){

	fun updateValue(){
		value=(25*speed)+(20*armor)+(15*strength)+(maxHealth)
	}
//	@get:JsonGetter("webDescription")


		@JsonGetter("webDescription")
		fun getWebDescription() = name + "<br/>Health: $currentHealth/$maxHealth<br/>speed: $speed<br/>strength: $strength<br/>armor: $armor<br/>Weapon: ${weapon.description}<br/><br/>Gold for beating: $value"

	 override fun toString(): String{
		 return getWebDescription()
	 }

 }
