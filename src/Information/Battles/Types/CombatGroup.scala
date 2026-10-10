package Information.Battles.Types

import ProxyBwapi.UnitInfo.UnitInfo

// Identity denotes units allowed to rely on each other, independently of spatial clustering.
final class CombatGroup(val name: String)

object CombatGroup {
  val army: CombatGroup = new CombatGroup("Army")
  @volatile private var _revision: Long = 0
  def revision: Long = _revision
  def invalidate(): Unit = { _revision += 1 }
  def of(unit: UnitInfo): CombatGroup = unit.friendly.flatMap(_.squad).map(_.combatGroup).getOrElse(army)
}
