package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterIgnoreScouts extends TargetFilter {
  
  // If we need to defend from real threats,
  // ignore scouting workers
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val atHome = actor.base.exists(_.owner.isUs)
    lazy val fortifying = actor.agent.toForm.isDefined
    lazy val targetingWorker = target.unitClass.isWorker
    lazy val combatThreats = actor.matchups.enemies.exists(e => e.unitClass.attacksGround && ! e.unitClass.isWorker)
    lazy val targetViolent = target.isBeingViolent
    val output = ! atHome  || ! targetingWorker || ! combatThreats || targetViolent
    output
  }
  
}
