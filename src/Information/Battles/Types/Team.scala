package Information.Battles.Types

import Information.Battles.Prediction.Skimulation.SkimulationTeam
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.UnitGroup

abstract class Team(val battle: Battle, val units: Seq[UnitInfo]) extends UnitGroup with SkimulationTeam  {

  final def groupUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us

  lazy val hasDetection: Boolean = asGroup.detectors.nonEmpty || (enemy && MacroFacts.enemyHasShown(Terran.SpiderMine, Terran.Comsat, Terran.SpellScannerSweep))
  
  protected def asGroup: UnitGroup = asInstanceOf[UnitGroup]

  val vanguardAll = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        asGroup.attackers,
        units))(_.pixelDistanceSquared(opponent.asGroup.attackCentroidKey))
    .map(_.pixel)
    .getOrElse(With.scouting.enemyThreatOrigin.center))

  val vanguardAir = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        asGroup.attackers.view.filter(_.flying),
        units.view.filter(_.flying)))(_.pixelDistanceTravelling(opponent.asGroup.attackCentroidKey))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  val vanguardGround = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        asGroup.attackers.view.filterNot(_.flying),
        units.view.filterNot(_.flying)))(_.pixelDistanceTravelling(opponent.asGroup.attackCentroidGround))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  def vanguardKey: Cache[Pixel] = if (asGroup.hasGround) vanguardGround else vanguardAir
}
