package Information.Battles.Types

import Information.Battles.Simulation.Construction.BattleSimulation
import Mathematics.Pixels.Pixel
import Performance.Caching.CacheFrame

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  var simulations: Iterable[BattleSimulation] = Vector.empty
  
  def focus     : Pixel = us.vanguard.midpoint(enemy.vanguard)
  def groups    : Iterable[BattleGroup] = Vector(us, enemy)
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
      .toVector
      .sortBy(simulation => ! simulation.us.tactics.has(Tactics.Workers.Ignore))
      .sortBy(simulation => ! simulation.us.tactics.has(Tactics.Wounded.Fight))
      .sortBy(simulation => simulation.us.tactics.has(Tactics.Movement.Kite))
      .sortBy(simulation => simulation.us.tactics.has(Tactics.Movement.Charge))
      .sortBy(simulation => simulation.us.lostValue - simulation.enemy.lostValue)
      .headOption)
}
