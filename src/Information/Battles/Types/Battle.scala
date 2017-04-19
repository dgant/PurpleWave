package Information.Battles.Types

import Information.Battles.Evaluation.BattleEvaluation
import Information.Battles.Simulation.Construction.BattleSimulation
import Mathematics.Pixels.Pixel
import Performance.Caching.CacheFrame

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  us.battle       = this
  enemy.battle    = this
  us.opponent     = enemy
  enemy.opponent  = us
  
  def focus: Pixel = us.vanguard.midpoint(enemy.vanguard)
  def groups: Iterable[BattleGroup] = Vector(us, enemy)
  def happening: Boolean = us.units.nonEmpty && enemy.units.nonEmpty && (us.units.exists(_.canAttackThisSecond) || enemy.units.exists(_.canAttackThisSecond))
  
  var evaluations: Vector[BattleEvaluation] = Vector.empty
  var simulations: Vector[BattleSimulation] = Vector.empty
  
  def consensusTactics:TacticsOptions = consensusTacticsCache.get
  private val consensusTacticsCache = new CacheFrame(() => {
    //Very TODO
    us.tacticsAvailable.head
  })
}
