package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.UnitGroup

class Team(val battle: Battle, val units: Vector[UnitInfo]) extends UnitGroup {

  final def groupUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us

  val vanguard = new Cache(() =>
    Maff.minBy(attackers)(_.pixelDistanceSquared(opponent.centroidKey))
    .orElse(Maff.minBy(units)(_.pixelDistanceSquared(opponent.centroidKey)))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))
}
