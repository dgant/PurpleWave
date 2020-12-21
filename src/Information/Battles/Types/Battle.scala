package Information.Battles.Types

import Information.Geography.Types.Zone
import ProxyBwapi.UnitInfo.UnitInfo

abstract class Battle(val us: Team, val enemy: Team) {
  us.battle = this
  enemy.battle = this
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy

  lazy val zones: Set[Zone] = teams.map(_.zones).reduce(_ ++ _)
}
