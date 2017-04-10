package Information.Battles

import Information.Battles.Simulation.Construction.BattleSimulation
import Information.Battles.Simulation.Tactics.{TacticMovement, TacticWounded}
import Performance.Caching.CacheFrame
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
  
  def bestSimulationResult:Option[BattleSimulation] = bestSimulationResultCache.get
  
  // Pick the "best" simulation result
  // Go for best value, but tiebreak by preferring to fight! (And to keep mining)
  private val bestSimulationResultCache = new CacheFrame(() =>
    simulations
      .toList
      .sortBy(simulation => simulation.us.tactics.workers   != TacticWounded.Ignore)
      .sortBy(simulation => simulation.us.tactics.wounded   != TacticWounded.Ignore)
      .sortBy(simulation => simulation.us.tactics.movement  != TacticMovement.Kite)
      .sortBy(simulation => simulation.us.tactics.movement  != TacticMovement.Charge)
      .sortBy(simulation => simulation.us.lostValue - simulation.enemy.lostValue)
      .headOption)
}
