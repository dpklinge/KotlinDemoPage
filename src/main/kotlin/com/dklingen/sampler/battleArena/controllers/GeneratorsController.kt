package com.dklingen.sampler.battleArena.controllers

import com.dklingen.sampler.battleArena.robot.DifficultyClass
import com.dklingen.sampler.battleArena.robot.RobotFactory
import org.springframework.beans.factory.annotation.Autowired
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class EnemyGeneratorController {

	@Autowired
	private lateinit var roboFactory: RobotFactory

	@GetMapping("generateCurrentLevelEnemy")
	@ResponseBody
	fun getCurrentLevelRobot(req: HttpServletRequest, session: HttpSession): String {
		val level = session.getAttribute("enemyLevel") as? DifficultyClass ?: DifficultyClass.WEAK
		return when (level) {
			DifficultyClass.WEAK -> getEasyRobot(req, session)
			DifficultyClass.AVERAGE -> getMediumRobot(req, session)
			DifficultyClass.STRONG -> getHardRobot(req, session)
			DifficultyClass.LEGENDARY -> getLegendaryRobot(req, session)
			DifficultyClass.GODLIKE -> getGodRobot(req, session)
			else -> "????"
		}
	}

	@GetMapping("generateEasyEnemy")
	@ResponseBody
	fun getEasyRobot(req: HttpServletRequest, session: HttpSession): String {
		session.setAttribute("enemyLevel", DifficultyClass.WEAK)
		val robot = roboFactory.generateEasyEnemy()
		session.setAttribute("enemyRobot", robot)
		return robot.toString()
	}

	@GetMapping("generateHardEnemy")
	@ResponseBody
	fun getHardRobot(req: HttpServletRequest, session: HttpSession): String {
		session.setAttribute("enemyLevel", DifficultyClass.STRONG)
		val robot = roboFactory.generateHardEnemy()
		session.setAttribute("enemyRobot", robot)
		return robot.toString()
	}

	@GetMapping("generateMediumEnemy")
	@ResponseBody
	fun getMediumRobot(req: HttpServletRequest, session: HttpSession): String {
		session.setAttribute("enemyLevel", DifficultyClass.AVERAGE)
		val robot = roboFactory.generateMediumEnemy()
		session.setAttribute("enemyRobot", robot)
		return robot.toString()
	}

	@GetMapping("generateLegendaryEnemy")
	@ResponseBody
	fun getLegendaryRobot(req: HttpServletRequest, session: HttpSession): String {
		session.setAttribute("enemyLevel", DifficultyClass.LEGENDARY)
		val robot = roboFactory.generateLegendaryEnemy()
		session.setAttribute("enemyRobot", robot)
		return robot.toString()
	}

	@GetMapping("generateGodEnemy")
	@ResponseBody
	fun getGodRobot(req: HttpServletRequest, session: HttpSession): String {
		session.setAttribute("enemyLevel", DifficultyClass.GODLIKE)
		val robot = roboFactory.generateGodlikeEnemy()
		session.setAttribute("enemyRobot", robot)
		return robot.toString()
	}
}

