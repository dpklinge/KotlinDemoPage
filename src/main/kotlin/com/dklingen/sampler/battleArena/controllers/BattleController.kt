package com.dklingen.sampler.battleArena.controllers

import com.dklingen.sampler.battleArena.Battle
import com.dklingen.sampler.battleArena.BattleResults
import com.dklingen.sampler.battleArena.robot.BattleRobot
import com.dklingen.sampler.battleArena.robot.DifficultyClass
import com.dklingen.sampler.battleArena.weapon.WeaponStore
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping


@Controller
class BattleController(@Autowired val  battle: Battle) {

	@GetMapping("battle")
	fun doGet(model: Model, session: HttpSession, req: HttpServletRequest): String {
		val playerRobot = session.getAttribute("robot") as BattleRobot
		val enemyRobot = session.getAttribute("enemyRobot") as BattleRobot
		val results = battle.doBattle(playerRobot, enemyRobot)

		if (results.isWin) {
			checkCurrentWinLevel(session, enemyRobot)
			val money = (session.getAttribute("money") as Int) + results.attackerWinnings
			results.addToLog("As the victor, you won ${results.attackerWinnings}!")
			session.setAttribute("robot", playerRobot)
			session.setAttribute("money", money)
			req.setAttribute("results", results)
		} else {
			results.addToLog("Your robot was utterly destroyed!<br/>")
			val scrapValue =  (playerRobot.value * 0.4).toInt() + (playerRobot.weapon.value * 0.4).toInt()
			results.addToLog("You salvage $scrapValue worth of scrap and your weapon from the wreckage.<br/>")
			results.addToLog("Enemy health: ${enemyRobot.currentHealth}/${enemyRobot.maxHealth}")
			req.setAttribute("robot", playerRobot)
			session.setAttribute("robot", null)
			session.setAttribute("money", (session.getAttribute("money") as Int) + scrapValue)
			req.setAttribute("results", results)
		}
		model["robot"] = playerRobot
		model["enemyRobot"] = enemyRobot
		return "battleScreen"
	}

	@GetMapping("fight")
	fun doGet(model: Model, req: HttpServletRequest, session: HttpSession): String {
		System.out.println("Getting arena")
		return if (session.getAttribute("robot") == null) {
			System.out.println("No robot found")
			req.setAttribute("noRobotError", "You need a robot before you can fight!")
			"redirect:/home"
		} else {
			System.out.println("Robot found: "+ session.getAttribute("robot"))
			model.set("robot", session.getAttribute("robot"))
			"arena"
		}
	}

	fun checkCurrentWinLevel(session: HttpSession, enemyRobot: BattleRobot) {
		val enemyLevel = session.getAttribute("enemyLevel") as DifficultyClass
		val currentLevel = session.getAttribute("victoryLevel") as? DifficultyClass
		val weaponStore = session.getAttribute("weaponStore") as? WeaponStore

		if (enemyRobot.name.contains("Boss ")) {
			weaponStore?.addBossLevel(enemyLevel)
		}

		if (currentLevel == null || enemyLevel.difficultyLevel > currentLevel.difficultyLevel) {
			session.setAttribute("victoryLevel", enemyLevel)
		}
	}
}
