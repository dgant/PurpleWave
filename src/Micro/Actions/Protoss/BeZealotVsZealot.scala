package Micro.Actions.Protoss

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes

import scala.language.postfixOps

object BeZealotVsZealot extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.frame < Minutes(5)()
      && Protoss.Zealot(unit)
      && With.enemies.exists(_.isProtoss)
      && unit.matchups.threatsInPixels(128).forall(_.isAny(Protoss.Zealot, Protoss.Probe)))

  override def perform(unit: FriendlyUnitInfo): Unit = {
    val zealots = unit.matchups.allUnits.filter(Protoss.Zealot)

    //val scores = zealots.map(z => (z, Maff.max(z.matchups.targetsInRange.map(_.matchups.threatsInRange.length)).getOrElse(0) - z.matchups.threatsInRange.length)).toMap

    if (unit.matchups.threatsInPixels(24).filter(Protoss.Zealot).count(_.presumptiveTarget.contains(unit)) > 1) {
      unit.agent.fightReason = "Outnumbered"
      unit.agent.shouldFight = false
      return
    }
    if (unit.totalHealth < 14 && unit.matchups.threatsInPixels(64).exists(t => t.totalHealth > 14 || t.cooldownLeft < unit.cooldownLeft)) {
      unit.agent.fightReason = "Dying"
      unit.agent.shouldFight = false
      return
    }

    val obviousTarget = Maff.minBy(unit.matchups.targetsInPixels(16))(t => t.totalHealth / (1.0 + t.matchups.threatsInRange.count(unit!=)))
    unit.agent.toAttack = obviousTarget
    val target = obviousTarget.orElse(unit.matchups.targetNearest)

    if (target.exists(t => unit.matchups.threatsInPixels(unit.pixelsToGetInRange(t) + 32).forall(t==) && unit.totalHealth * (1 + unit.confidence11) > t.totalHealth)) {
      unit.agent.fightReason  = "Duelling"
      unit.agent.shouldFight  = true
      unit.agent.toAttack     = target
      return
    }

    if ( ! unit.matchups.threatsInPixels(64).exists(_.presumptiveTarget.contains(unit))
      && target.exists(enemy =>
      enemy.presumptiveTarget.exists(ally =>
        ally.friendly.exists(_.agent.shouldFight)
        || unit.totalHealth * (1 + unit.confidence11) > enemy.totalHealth))) {
      unit.agent.fightReason  = "Outnumbering"
      unit.agent.shouldFight  = true
      unit.agent.toAttack     = target
    }
  }
}
