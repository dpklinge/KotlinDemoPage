package com.dklingen.sampler.battleArena.controllers

import com.dklingen.sampler.battleArena.robot.BattleRobot
import com.dklingen.sampler.battleArena.robot.DifficultyClass
import com.dklingen.sampler.battleArena.weapon.RobotWeapon
import com.dklingen.sampler.battleArena.weapon.WeaponStore
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.lang.RuntimeException


import java.time.temporal.ChronoUnit

@Controller
public class ShopController(
	var firstVisit: Boolean = true
)
{
	@GetMapping("/equip")
	fun doEquip(model: Model, session: HttpSession): String {
		if (session.getAttribute("robot") == null) {
			model.addAttribute("noRobotError", "Go build a robot before you try to equip it!")
			return "redirect:/home"
		}
		val weaponStore = session.getAttribute("weaponStore") as? WeaponStore?: throw RuntimeException("Error retrieving weapon store")
		val achievedLevel: DifficultyClass = session.getAttribute("victoryLevel") as DifficultyClass
		val weaponList: List<RobotWeapon> 
		if(firstVisit|| achievedLevel != weaponStore.storeLevel){
			firstVisit=false
			weaponList = weaponStore.forceUpdate(achievedLevel)
		}else{
			weaponList = weaponStore.checkForNewInventory(achievedLevel)
			
		}
		session.setAttribute("timeToUpdate", weaponStore.time.plus(2L, ChronoUnit.MINUTES).toLocalTime())
		session.setAttribute("weaponsForSale", weaponList)
		model["weaponsForSale"] = weaponList
		model["robot"] = session.getAttribute("robot")
		model["inventory"] = session.getAttribute("inventory")
		model["money"] = session.getAttribute("money")
		model["timeToUpdate"] = weaponStore.time.plus(2L, ChronoUnit.MINUTES).toLocalTime()
		return "equip"
	}


	@PostMapping("buyWeapon")
	fun buyWeapon(model: Model, session: HttpSession, req: HttpServletRequest): String {
		val index = req.getParameter("weaponIndex").toInt()
		val weaponsForSale = session.getAttribute("weaponsForSale") as MutableList<RobotWeapon>
		val inventory = session.getAttribute("inventory") as MutableList<RobotWeapon>
		var money = session.getAttribute("money") as Int
		val weaponStore = session.getAttribute("weaponStore") as? WeaponStore?: throw RuntimeException("Error retrieving weapon store")

		if (weaponsForSale[index] != weaponStore.inventory[index]) {
			model.addAttribute("error", "Too slow! Weapons updated!")
		} else if (money > weaponsForSale[index].value) {
			val robot = session.getAttribute("robot") as BattleRobot
			inventory.add(0, robot.weapon)

			robot.weapon = weaponsForSale[index]
			money -= weaponsForSale[index].value
			weaponsForSale.removeAt(index)
			session.setAttribute("inventory", inventory)
			session.setAttribute("money", money)
		} else {
			model.addAttribute("error", "Not enough money to purchase!")
		}
		model["weaponsForSale"] = weaponsForSale
		model["robot"] = session.getAttribute("robot")
		model["inventory"] = session.getAttribute("inventory")
		model["money"] = session.getAttribute("money")
		model["timeToUpdate"] = session.getAttribute("timeToUpdate")
		return "equip"
	}

	@PostMapping("equipWeapon")
	fun equipWeapon(@RequestParam weaponIndex: String, model: Model, session: HttpSession): String {
		val index = weaponIndex.toInt()
		val inventory = session.getAttribute("inventory") as MutableList<RobotWeapon>
		val robot = session.getAttribute("robot") as BattleRobot
		val oldWeapon = robot.weapon
		robot.weapon = inventory[index]
		inventory.removeAt(index)
		inventory.add(0, oldWeapon)

		session.setAttribute("robot", robot)
		session.setAttribute("inventory", inventory)
		model["weaponsForSale"] = session.getAttribute("weaponsForSale")
		model["robot"] = session.getAttribute("robot")
		model["inventory"] = session.getAttribute("inventory")
		model["money"] = session.getAttribute("money")
		model["timeToUpdate"] = session.getAttribute("timeToUpdate")
		return "redirect:/equip"
	}

	@PostMapping("sellWeapon")
	fun sellWeapon(@RequestParam weaponIndex: String, model: Model, session: HttpSession): String {
		val index = weaponIndex.toInt()
		val inventory = session.getAttribute("inventory") as MutableList<RobotWeapon>
		val robot = session.getAttribute("robot") as BattleRobot

		if (inventory[index] == robot.weapon) {
			model.addAttribute("sellError", "You cannot sell your equipped weapon!")
			return "equip"
		}

		var money = session.getAttribute("money") as Int
		money = (money + money * 0.4).toInt()

		inventory.removeAt(index)

		session.setAttribute("money", money)
		session.setAttribute("robot", robot)
		session.setAttribute("inventory", inventory)
		model["weaponsForSale"] = session.getAttribute("weaponsForSale")
		model["robot"] = session.getAttribute("robot")
		model["inventory"] = session.getAttribute("inventory")
		model["money"] = session.getAttribute("money")
		model["timeToUpdate"] = session.getAttribute("timeToUpdate")
		return "redirect:/equip"
	}
}
