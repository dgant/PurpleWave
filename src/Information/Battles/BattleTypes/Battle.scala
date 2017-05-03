package Information.Battles.BattleTypes

import Information.Battles.Estimation.{BattleEstimation, BattleEstimationResult}
import Information.Battles.EvaluateTactics
import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With
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
  def estimation(tactics:TacticsOptions):Option[BattleEstimationResult] = estimations.find(_.tacticsUs == tactics).map(_.result)
  def estimation:BattleEstimationResult = estimation(bestTactics).getOrElse(new BattleEstimationResult)
  
  var lastBestTactics:TacticsOptions = new TacticsOptions
  private var currentBestTactics:TacticsOptions = new TacticsOptions
  private var lastBestTacticsUpdatedFrame = 0
  def bestTactics:TacticsOptions = {
    if (lastBestTacticsUpdatedFrame < With.frame) {
      lastBestTacticsUpdatedFrame = With.frame
      lastBestTactics = currentBestTactics
    }
    currentBestTactics = bestTacticsCache.get
    currentBestTactics
  }
  private val bestTacticsCache = new CacheFrame(() => EvaluateTactics.best(this))
  
  def rankedTactics:Vector[TacticsOptions] = rankedTacticsCache.get
  private val rankedTacticsCache = new CacheFrame(() => EvaluateTactics.sort(this))
}
