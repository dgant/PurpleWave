package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    (
      target.unitClass.attacksOrCastsOrDetectsOrTransports
      || actor.battle.forall(_.predictionAttack.deathsUs == 0)
      || ! actor.battle.exists(_.teamOf(actor).engaged()))
  }
}
