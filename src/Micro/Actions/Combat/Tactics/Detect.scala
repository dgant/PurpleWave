package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Detect extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.isDetector
    && unit.teammates.exists(_.canAttack)
  )

  private def canEventuallyCloak(unit: UnitInfo): Boolean = {
    unit.isAny(Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Zerg.Lurker)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    val spookiestSpooky =
      pickBestSpooky(unit, unit.squad.map(_.enemies.filter(_.effectivelyCloaked)).getOrElse(Iterable.empty)).orElse(
        pickBestSpooky(unit, unit.squad.map(_.enemies.filter(_.cloakedOrBurrowed)).getOrElse(Iterable.empty))).orElse(
          pickBestSpooky(unit, unit.squad.map(_.enemies.filter(canEventuallyCloak)).getOrElse(Iterable.empty))).orElse(
          if (unit.agent.canFocus) None else
            pickBestSpooky(unit, unit.matchups.enemies.filter(_.effectivelyCloaked)).orElse(
              pickBestSpooky(unit, unit.matchups.enemies.filter(_.cloakedOrBurrowed)).orElse(
                pickBestSpooky(unit, unit.matchups.enemies.filter(canEventuallyCloak)))))

    if (spookiestSpooky.isEmpty) {
      return
    }

    val spooky = spookiestSpooky.get
    val ghostbusters = spooky.matchups.enemies.filter(e => e.canMove && (if (spooky.flying) e.unitClass.attacksAir else e.unitClass.attacksGround))
    val ghostbuster = ByOption.minBy(ghostbusters)(_.framesBeforeAttacking(spooky))

    unit.agent.toTravel = ghostbuster.map(_.pixel).orElse(Some(spooky.pixel))

    if (unit.matchups.framesOfSafety <= 0 && spookiestSpooky.forall(s =>
      s.detected
      || (s.matchups.targetsInRange.isEmpty && s.matchups.threatsInRange.isEmpty))) {
      Retreat.delegate(unit)
    }
    else {
      With.commander.move(unit)
    }
  }

  def pickBestSpooky(detector: FriendlyUnitInfo, spookies: Iterable[UnitInfo]): Option[UnitInfo] = {
    ByOption.minBy(spookies)(s =>
      ByOption
        .min(s.matchups.targets.map(s.pixelDistanceSquared))
        .getOrElse(s.pixelDistanceSquared(detector)))
  }
}
