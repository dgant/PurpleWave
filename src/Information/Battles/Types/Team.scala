package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.UnitGroup

class Team(val battle: Battle, val units: Vector[UnitInfo]) extends UnitGroup {

  final def groupUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us

  val vanguardAll = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers,
        units))(_.pixelDistanceSquared(opponent.centroidKey))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))

  val vanguardAir = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers.view.filter(_.flying),
        units.view.filter(_.flying)))(_.pixelDistanceTravelling(opponent.centroidKey))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  val vanguardGround = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers.view.filterNot(_.flying),
        units.view.filterNot(_.flying)))(_.pixelDistanceTravelling(opponent.centroidGround))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  def vanguardKey: Cache[Pixel] = if (hasGround) vanguardGround else vanguardAir
}
