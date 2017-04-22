package Information.Battles.BattleTypes

import Information.Battles.Estimation.BattleEstimation
import Information.Battles.EvaluateTactics
import Information.Battles.Simulation.Construction.BattleSimulation
import Information.Battles.TacticsTypes.TacticsOptions
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
  
  var estimations: Vector[BattleEstimation] = Vector.empty
  var simulations: Vector[BattleSimulation] = Vector.empty
  
  def estimation(tactics:TacticsOptions):Option[BattleEstimation] = estimations.find(_.tactics == tactics)
  def simulation(tactics:TacticsOptions):Option[BattleSimulation] = simulations.find(_.tactics == tactics)
  
  def consensusTactics:TacticsOptions = consensusTacticsCache.get
  private val consensusTacticsCache = new CacheFrame(() => {
    EvaluateTactics.best(this)
  })
}
