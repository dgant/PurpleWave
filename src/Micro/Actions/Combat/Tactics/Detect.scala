package Micro.Actions.Combat.Tactics

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
      pickBestSpooky(unit, unit.enemiesBattle.filter(_.effectivelyCloaked))).orElse(
      pickBestSpooky(unit, unit.enemiesBattle.filter(_.cloakedOrBurrowed))).orElse(
      pickBestSpooky(unit, unit.enemiesBattle.filter(canEventuallyCloak)))

    lazy val minesweepPoint = ByOption
      .minBy(unit.alliesSquad.view.filter(_.canAttack))(_.pixelDistanceTravelling(unit.agent.destination))
      .map(_.pixel.project(unit.agent.destination, unit.sightPixels))

    val spookiestPixel = spookiestSpooky.map(_.pixel).orElse(minesweepPoint)

    if (spookiestPixel.isEmpty) return

    val ghostbusters = spookiestSpooky.map(s => s.matchups.enemies.filter(e => e.canMove && e.attacksAgainst(s) > 0))
    val ghostbuster = ghostbusters.flatMap(g => ByOption.minBy(g)(_.framesBeforeAttacking(spookiestSpooky.get)))

    val idealDistance = Math.max(0, unit.sightPixels - Math.min(0, unit.matchups.pixelsOfEntanglement))
    val center = ghostbuster.map(_.pixel).getOrElse(unit.agent.origin)
    unit.agent.toTravel = Some(spookiestPixel.get.project(center, idealDistance))

    val safetyPixels = if (unit.is(Protoss.Observer)) -16 else -32 * 5
    if (unit.matchups.pixelsOfEntanglement > safetyPixels) {
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
}
