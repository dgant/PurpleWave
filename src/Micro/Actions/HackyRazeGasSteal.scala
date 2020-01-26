package Micro.Actions
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object HackyRazeGasSteal extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = true

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val gasSteal = unit.matchups.targetsInRange.find(u => u.unitClass.isGas && u.zone.owner.isUs)
    if (gasSteal.isDefined) {
      unit.agent.toAttack = gasSteal
      Attack.delegate(unit)
    }
  }
}
