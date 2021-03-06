package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.PurpleMath
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
      pickBestSpooky(unit, if (unit.matchups.anchor.exists(_.isAny(Terran.Battlecruiser, Protoss.Carrier, Zerg.Mutalisk, Zerg.Devourer))) unit.enemiesSquad.filter(Terran.Wraith) else Seq.empty).orElse(
      pickBestSpooky(unit, if (unit.matchups.anchor.exists(_.isAny(Terran.Battlecruiser, Protoss.Carrier, Zerg.Mutalisk, Zerg.Devourer))) unit.enemiesBattle.filter(Terran.Wraith) else Seq.empty)).orElse(
      pickBestSpooky(unit, unit.enemiesSquad.filter(_.effectivelyCloaked))).orElse(
      pickBestSpooky(unit, unit.enemiesSquad.filter(_.cloakedOrBurrowed))).orElse(
      pickBestSpooky(unit, unit.enemiesSquad.filter(canEventuallyCloak))).orElse(
      pickBestSpooky(unit, unit.enemiesBattle.filter(_.effectivelyCloaked))).orElse(
      pickBestSpooky(unit, unit.enemiesBattle.filter(_.cloakedOrBurrowed))).orElse(
      pickBestSpooky(unit, unit.enemiesBattle.filter(canEventuallyCloak)))

    lazy val minesweepingNeeded = With.unitsShown.any(Terran.SpiderMine, Terran.Vulture, Terran.Factory, Terran.Goliath, Terran.SiegeTankUnsieged, Terran.SiegeTankUnsieged, Protoss.Arbiter, Protoss.DarkTemplar, Zerg.Lurker)
    lazy val minesweepPoint = if (minesweepingNeeded) ByOption
      .minBy(unit.alliesSquad.view.filter(_.canAttack))(_.pixelDistanceTravelling(unit.agent.destination))
      .map(_.pixel.project(unit.agent.destination, unit.sightPixels)) else None

    val spookiestPixel = spookiestSpooky.map(_.pixel).orElse(minesweepPoint)

    if (spookiestPixel.isEmpty) return

    val ghostbusters = spookiestSpooky.map(s => s.matchups.enemies.filter(e => e.canMove && e.attacksAgainst(s) > 0))
    val ghostbuster = ghostbusters.flatMap(g => ByOption.minBy(g)(_.framesBeforeAttacking(spookiestSpooky.get)))

    val idealDistance = PurpleMath.clamp(
      if ( ! unit.cloaked || unit.matchups.enemyDetectors.nonEmpty ||With.enemies.exists(_.isTerran)) unit.matchups.pixelsOfEntanglement else 0,
      unit.sightPixels / 2,
      unit.sightPixels - 24)

    val center = ghostbuster.map(_.pixel).getOrElse(unit.agent.origin)
    unit.agent.toTravel = Some(spookiestPixel.get.project(center, idealDistance))

    val safetyPixels = if (unit.is(Protoss.Observer)) -48 else -32 * 5
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
