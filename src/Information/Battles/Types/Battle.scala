package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

abstract class Battle(val us: Team, val enemy: Team) {
  us.battle = this
  enemy.battle = this
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
}
