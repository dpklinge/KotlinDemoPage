package com.dklingen.sampler.battleArena

import com.dklingen.sampler.battleArena.robot.BattleRobot

 class BattleResults (
	var battleLog: String="",
	val attacker:BattleRobot,
	val defender: BattleRobot,
	var attackerWinnings: Int = defender.value,
	var defenderWinnings: Int = attacker.value,
	var isWin:Boolean = false
 ) {

	fun addToLog( addition: String) {
		battleLog += "$addition<br/>"
	}

	 fun updateWinnings(attacker: BattleRobot, amount: Int) {
		if(attacker == this.attacker){
			attackerWinnings += amount
		}else{
			defenderWinnings += amount
		}
	 }

	 fun getWinnings(attacker: BattleRobot): Int {
		 return if(attacker == this.attacker){
			 attackerWinnings
		 }else{
			 defenderWinnings
		 }
	 }

 }
