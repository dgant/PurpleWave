package Information.Battles.Types

import Information.Battles.Prediction.Skimulation.SkimulationTeam
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.UnitGroup

class Team(val battle: Battle, val units: Seq[UnitInfo]) extends UnitGroup with SkimulationTeam {

  final def groupUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us

  lazy val hasDetection: Boolean = detectors.nonEmpty || (enemy && MacroFacts.enemyHasShown(Terran.SpiderMine, Terran.Comsat, Terran.SpellScannerSweep))

  lazy val vanguardAll = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers,
        units))(_.pixelDistanceSquared(opponent.centroidKey))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))

  lazy val vanguardAir = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers.view.filter(_.flying),
        units.view.filter(_.flying)))(_.pixelDistanceTravelling(opponent.centroidKey))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  lazy val vanguardGround = new Cache(() =>
    Maff.minBy(
      Maff.orElse(
        attackers.view.filterNot(_.flying),
        units.view.filterNot(_.flying)))(_.pixelDistanceTravelling(opponent.centroidGround))
    .map(_.pixel)
    .getOrElse(vanguardAll()))

  def vanguardKey: Cache[Pixel] = if (hasGround) vanguardGround else vanguardAir
}
