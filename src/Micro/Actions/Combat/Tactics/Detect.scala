package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Detect extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.unitClass.isDetector

  private def canEventuallyCloak(unit: UnitInfo): Boolean = {
    unit.isAny(Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Zerg.Lurker)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    val spookiestSpooky =
      pickBestSpooky(unit, unit.enemiesSquad.filter(_.effectivelyCloaked)).orElse(
        pickBestSpooky(unit, unit.enemiesSquad.filter(_.cloakedOrBurrowed))).orElse(
          pickBestSpooky(unit, unit.enemiesSquad.filter(canEventuallyCloak))).orElse(
            pickBestSpooky(unit, unit.matchups.enemies.filter(_.effectivelyCloaked))).orElse(
              pickBestSpooky(unit, unit.matchups.enemies.filter(_.cloakedOrBurrowed))).orElse(
                pickBestSpooky(unit, unit.matchups.enemies.filter(canEventuallyCloak)))

    val spookiestPixel = spookiestSpooky.map(_.pixel).orElse(minesweepPoint(unit))

    if (spookiestPixel.isEmpty) return

    val ghostbusters = spookiestSpooky.map(s => s.matchups.enemies.filter(e => e.canMove && e.attacksAgainst(s) > 0))
    val ghostbuster = ghostbusters.flatMap(g => ByOption.minBy(g)(_.framesBeforeAttacking(spookiestSpooky.get)))

    val idealDistance = Math.max(0, unit.sightPixels - Math.min(0, unit.matchups.pixelsOfEntanglement))
    val center = ghostbuster.map(_.pixel).getOrElse(unit.agent.origin)
    unit.agent.toTravel = Some(spookiestPixel.get.project(center, idealDistance))

    if (unit.matchups.pixelsOfEntanglement > -16) {
      Retreat.delegate(unit)
    } else {
      Commander.move(unit)
    }
  }

  def pickBestSpooky(detector: FriendlyUnitInfo, spookies: Iterable[UnitInfo]): Option[UnitInfo] = {
    ByOption.minBy(spookies)(s =>
      ByOption
        .min(s.matchups.targets.map(s.pixelDistanceSquared))
        .getOrElse(s.pixelDistanceSquared(detector)))
  }

  def minesweepPoint(detector: FriendlyUnitInfo): Option[Pixel] = {
    if (detector.alliesSquad.forall(_.flying)) return None
    if (With.unitsShown.allEnemies(Terran.Vulture) == 0
      && With.unitsShown.allEnemies(Terran.Wraith) == 0
      && With.unitsShown.allEnemies(Protoss.DarkTemplar) == 0
      && With.unitsShown.allEnemies(Protoss.Arbiter) == 0
      && With.unitsShown.allEnemies(Protoss.TemplarArchives) == 0
      && With.unitsShown.allEnemies(Protoss.ArbiterTribunal) == 0
      && With.unitsShown.allEnemies(Zerg.Lurker) == 0) return  None
    val destination = detector.agent.destination.nearestWalkablePixel
    ByOption.minBy(detector.alliesSquad)(_.pixelDistanceTravelling(destination)).map(_.pixel.project(destination, detector.sightPixels))
  }
}
