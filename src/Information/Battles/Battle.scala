package Information.Battles

import Information.Battles.Simulation.Construction.BattleSimulation
import bwapi.Position
import Utilities.EnrichPosition._

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  var simulations: Iterable[BattleSimulation] = List.empty
  
  def focus     : Position = us.vanguard.midpoint(enemy.vanguard)
  def groups    : Iterable[BattleGroup] = List(us, enemy)
  def happening : Boolean =
    us.units.nonEmpty &&
    enemy.units.nonEmpty &&
    (us.units.exists(_.canAttackThisSecond) ||
    enemy.units.exists(_.canAttackThisSecond))
  
  def bestSimulationResult:Option[BattleSimulation] =
    if (simulations.isEmpty) None
    else Some(simulations.minBy(simulation => simulation.us.lostValue - simulation.enemy.lostValue))
}
