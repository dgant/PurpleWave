package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

object Detect extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.unitClass.isDetector

  private def canEventuallyCloak(unit: UnitInfo): Boolean = unit.isAny(Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Zerg.Lurker)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val otherDetectors = unit.squad.map(_.mobileDetectors).getOrElse(Seq.empty).filter(_.canMove).filterNot(unit==)
    val enemySources = Seq(unit.enemiesSquad, unit.enemiesBattle)
    val enemyFilters: Seq[UnitInfo => Boolean] = Seq(
      (u: UnitInfo) => u.effectivelyCloaked,
      (u: UnitInfo) => u.cloakedOrBurrowed,
      canEventuallyCloak)
    val spookiestSpooky = enemySources
      .map(_.filterNot(s => otherDetectors.exists(_.pixelsToSightRange(s) < unit.pixelsToSightRange(s))))
      .view
      .flatMap(source => enemyFilters.map(source.filter))
      .map(pickBestSpooky(unit, _))
      .find(_.nonEmpty)
      .flatten

    lazy val minesweepingNeeded = MacroFacts.enemyHasShown(Terran.SpiderMine, Terran.Vulture, Terran.Factory, Terran.Goliath, Terran.SiegeTankUnsieged, Terran.SiegeTankUnsieged, Protoss.Arbiter, Protoss.DarkTemplar, Zerg.Lurker)
    lazy val minesweepTarget = unit.squad.map(_.vicinity).getOrElse(With.scouting.enemyMuscleOrigin.center)
    lazy val minesweepPoint = MicroPathing.getGroundWaypointToPixel(unit.agent.destination, minesweepTarget)

    val spookiestPixel = spookiestSpooky.map(_.pixel).orElse(?(minesweepingNeeded, Some(minesweepPoint), None))
    if (spookiestPixel.isEmpty) return

    if (unit.matchups.pixelsEntangled < -64
      || (unit.cloaked
        && ! MacroFacts.enemyHasShown(Terran.Comsat, Terran.SpellScannerSweep)
        && unit.matchups.enemyDetectorDeepest.forall(_.pixelsToSightRange(unit) < 96))) {
      Commander.move(unit)
    } else {
      Retreat.delegate(unit)
    }
  }

  def pickBestSpooky(detector: FriendlyUnitInfo, spookies: Iterable[UnitInfo]): Option[UnitInfo] = {
    Maff.minBy(spookies)(s =>
      Maff
        .min(s.matchups.targets.map(s.pixelDistanceSquared))
        .getOrElse(s.pixelDistanceSquared(detector)))
  }
}
