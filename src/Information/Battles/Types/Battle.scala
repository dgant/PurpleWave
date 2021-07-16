package Information.Battles.Types

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

abstract class Battle(unitsUs: Vector[UnitInfo], unitsEnemy: Vector[UnitInfo]) {
  val us = new Team(this, unitsUs)
  val enemy = new Team(this, unitsEnemy)
  val teams: Vector[Team] = Vector(us, enemy)
  val frameCreated: Int = With.frame
}
