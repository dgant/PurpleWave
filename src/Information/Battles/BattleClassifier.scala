package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.Types.{Battle, BattleGlobal, BattleLocal, Team}
import Lifecycle.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class BattleClassifier {
    
  var global      : BattleGlobal                = new BattleGlobal(new Team(Vector.empty), new Team(Vector.empty))
  var byUnit      : Map[UnitInfo, BattleLocal]  = Map.empty
  var local       : Vector[BattleLocal]         = Vector.empty
  var lastUpdate  : Int                         = 0
  
  def all: Traversable[Battle] = local :+ global
  
  val clustering = new BattleClustering
  
  var lastEstimationCompletion = 0
  val estimationRuntimes = new mutable.Queue[Int]
  
  private var nextBattleGlobal: Option[BattleGlobal] = None
  private var nextBattlesLocal: Vector[BattleLocal] = Vector.empty
  def run() {
    val steps: Vector[() => Any] = (
      Vector[() => Any]()
      ++ Vector(() => BattleUpdater.run())
      ++ nextBattlesLocal.map(battle => () => battle.estimationSimulationAttack)
      ++ nextBattlesLocal.map(battle => () => battle.estimationSimulationSnipe)
      ++ nextBattlesLocal.map(battle => () => battle.shouldFight)
      ++ Vector(() => trackPerformance())
      ++ nextBattleGlobal.map(battle => () => battle.globalSafeToAttack)
      ++ Vector(() => replaceBattlesLocal())
      ++ Vector(() => replaceBattleGlobal())
      ++ Vector(() => runClustering())
    )
    for (step <- steps) {
      if ( ! With.performance.continueRunning) return
      step()
    }
  }
  
  def trackPerformance() {
    estimationRuntimes.enqueue(With.framesSince(lastEstimationCompletion))
    while (estimationRuntimes.sum > 24 * 30) estimationRuntimes.dequeue()
    lastEstimationCompletion = With.frame
  }
  
  def runClustering() {
    clustering.enqueue(With.units.all.filter(BattleClassificationFilters.isEligibleLocal))
    clustering.run()
  }
  
  private def replaceBattlesLocal() {
    local = nextBattlesLocal
    byUnit = local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
    nextBattlesLocal = clustering.clusters
      .map(cluster =>
        new BattleLocal(
          new Team(cluster.filter(_.isOurs).toVector),
          new Team(cluster.filter(_.isEnemy).toVector)))
      .filter(_.teams.forall(_.units.exists(_.canAttack)))
  }
  
  
  private def replaceBattleGlobal() {
    nextBattleGlobal.foreach(global = _)
    global = new BattleGlobal(
      new Team(asVectorUs     (With.units.ours  .filter(BattleClassificationFilters.isEligibleGlobal))),
      new Team(asVectorEnemy  (With.units.enemy .filter(BattleClassificationFilters.isEligibleGlobal))))
  }
  
  private def asVectorUs    (units: Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def asVectorEnemy (units: Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
}
