package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Agency.Commander.rightClick
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Heal extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = Terran.Medic(unit) && unit.battle.isDefined
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val targets       = Maff.orElse(unit.alliesSquad, unit.alliesBattle).filter(u => u.unitClass.isOrganic && ! Terran.Medic(u) && u.healers.forall(unit==))
    val target  = Maff.orElse(
      Maff.minBy(targets.filter(t => t.hitPoints < t.unitClass.maxHitPoints))(_.pixelDistanceSquared(unit)),
      Maff.minBy(targets)(_.pixelDistanceSquared(unit.agent.destinationNext())))

    if (target.nonEmpty) {
      With.coordinator.healing.heal(unit, target.head)
      Commander.rightClick(unit, target.head)
    }
  }
}
