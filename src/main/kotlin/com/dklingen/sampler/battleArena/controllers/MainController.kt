package com.dklingen.sampler.battleArena.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.dklingen.sampler.battleArena.robot.BattleRobot
import com.dklingen.sampler.battleArena.robot.DifficultyClass
import com.dklingen.sampler.battleArena.weapon.RobotWeapon
import com.dklingen.sampler.battleArena.weapon.WeaponFactory
import com.dklingen.sampler.battleArena.weapon.WeaponStore
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Random
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDateTime


@Controller
class RobotController (@Autowired val weaponFactory: WeaponFactory) {

	private val mapper = ObjectMapper()
	private val random = Random()

	@GetMapping("/build")
	fun robotBuilderGet(model: Model, session: HttpSession): String {
		model["robot"] = session.getAttribute("robot")
		model["money"] = session.getAttribute("money")
		return "build"
	}

	@PostMapping("/build")
	@ResponseBody
	fun robotBuilderPost(
		@RequestParam money: String,
		@RequestParam currentHealth: String,
		@RequestParam maxHealth: String,
		@RequestParam armor: String,
		@RequestParam speed: String,
		@RequestParam strength: String,
		@RequestParam name: String,
		session: HttpSession
	): String {
		val robot = session.getAttribute("robot") as BattleRobot

		robot.currentHealth = currentHealth.toInt()
		robot.maxHealth = maxHealth.toInt()
		robot.armor = armor.toInt()
		robot.speed = speed.toInt()
		robot.strength = strength.toInt()
		robot.name = name
		robot.updateValue()

		session.setAttribute("robot", robot)
		session.setAttribute("money", money.toInt())

		return mapper.writeValueAsString(robot)
	}

	@GetMapping("/home", "/")
	fun goHome(session: HttpSession, model: Model): String {
		var robot = session.getAttribute("robot") as? BattleRobot

		if (robot == null) {
			model.addAttribute("noRobotError", "You have been given a starter robot!")



			val inventory = (session.getAttribute("inventory") as? MutableList<RobotWeapon>) ?: mutableListOf()

			val weapon: RobotWeapon =  if (inventory.isNotEmpty()) {
				inventory[0]
			} else {
				val newWeapon = weaponFactory.getWeakWeapon(true)
				session.setAttribute("inventory", inventory)
				newWeapon
			}
			robot = BattleRobot(weapon = weapon)
			robot.name = RandomStringUtils.randomAlphanumeric(3, random.nextInt(5) + 3) + " Arena Contender"

			robot.updateValue()
			session.setAttribute("robot", robot)
		}

		if (session.getAttribute("money") == null) {
			session.setAttribute("money", 1000)
		}

		if(session.getAttribute("weaponStore") == null){
			session.setAttribute("weaponStore", WeaponStore(storeLevel = DifficultyClass.WEAK, time = LocalDateTime.now(), weaponFactory = weaponFactory))
		}

		if (session.getAttribute("victoryLevel") == null){
			session.setAttribute("victoryLevel", DifficultyClass.NONE)
		}

		if (session.getAttribute("inventory") == null){
			session.setAttribute("inventory", mutableListOf<RobotWeapon>())
		}

		return "index"
	}

	@GetMapping("newGame")
	fun newGame(session: HttpSession, req: HttpServletRequest): String {
		session.invalidate()
		req.setAttribute("newGame", "Your game has been restarted!")
		return "redirect:/home"
	}
}

