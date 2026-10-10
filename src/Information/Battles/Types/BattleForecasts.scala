package Information.Battles.Types

import ProxyBwapi.UnitInfo.UnitInfo

final case class BattleForecast(group: CombatGroup, battle: Battle)

// These prediction jobs never enter the spatial battle, division, or matchup indexes.
final class BattleForecasts(parent: Battle) {
  private val ownership = parent.us.units.map(u => (u, CombatGroup.of(u))).toVector
  private var checkedRevision = CombatGroup.revision
  private var valid = true
  val forecasts: Vector[BattleForecast] = {
    val groups = ownership.map(_._2).distinct
    if (groups.size <= 1) Vector(BattleForecast(groups.headOption.getOrElse(CombatGroup.army), parent))
    else groups.map(group => BattleForecast(group,
      new Battle(ownership.collect { case (u, g) if g == group => u }, parent.enemy.units, isGlobal = false)))
  }
  private val byUnit = forecasts.flatMap(f =>
    (if (forecasts.size == 1) f.battle.units else f.battle.us.units).map(_ -> f.battle)).toMap

  def forUnit(unit: UnitInfo): Option[Battle] = synchronized {
    // Check once per ownership change, not once per unit/frame. Invalidated jobs stay invalid.
    if (valid && checkedRevision != CombatGroup.revision) {
      valid = ownership.forall { case (u, group) => CombatGroup.of(u) == group }
      checkedRevision = CombatGroup.revision
    }
    if (valid) byUnit.get(unit) else None
  }
}
