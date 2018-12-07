package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.Types.{Battle, BattleGlobal, BattleLocal, Team}
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

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
      ++ Vector(() => local.foreach(updateBattle))
      ++ Vector(() => updateBattle(global))
      ++ Vector(() => nextBattlesLocal.foreach(updateBattle))
      ++ Vector(() => nextBattleGlobal.foreach(updateBattle))
      ++ (if (With.configuration.enableMCRS) Vector.empty else nextBattlesLocal.map(battle => () => battle.estimationSimulationAttack))
      ++ (if (With.configuration.enableMCRS) Vector.empty else nextBattlesLocal.map(battle => () => battle.estimationSimulationSnipe))
      ++ nextBattlesLocal.map(battle => () => battle.shouldFight)
      ++ Vector(() => trackPerformance())
      ++ nextBattleGlobal.map(battle => () => battle.globalSafeToAttack)
      ++ Vector(() => replaceBattlesLocal())
      ++ Vector(() => replaceBattleGlobal())
      ++ Vector(() => runClustering())
    )
    for (step <- steps) {
      if ( With.performance.continueRunning) {
        step()
      }
    }
  }
  
  def trackPerformance() {
    estimationRuntimes.enqueue(With.framesSince(lastEstimationCompletion))
    while (estimationRuntimes.sum > 24 * 2) estimationRuntimes.dequeue()
    lastEstimationCompletion = With.frame
  }
  
  def runClustering() {
    clustering.enqueue(With.units.playerOwned.filter(BattleClassificationFilters.isEligibleLocal))
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
      .filter(_.teams.forall(_.units.exists(u =>
        u.canAttack
        || u.unitClass.isSpellcaster
        || u.unitClass.isDetector
        || u.unitClass.isTransport)))
  }
  
  private def asVectorUs    (units: Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def asVectorEnemy (units: Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def replaceBattleGlobal() {
    nextBattleGlobal.foreach(global = _)
    global = new BattleGlobal(
      new Team(asVectorUs     (With.units.ours  .view.filter(BattleClassificationFilters.isEligibleGlobal))),
      new Team(asVectorEnemy  (With.units.enemy .view.filter(BattleClassificationFilters.isEligibleGlobal))))
  }
  
  private def updateBattle(battle: Battle) {
    battle.teams.foreach(group => {
      val airCentroid = PurpleMath.centroid(group.units.map(_.pixelCenter))
      val hasGround   = group.units.exists( ! _.flying)
      group.centroid  = ByOption
        .minBy(group.units.filterNot(_.flying && hasGround))(_.pixelDistanceSquared(airCentroid))
        .map(_.pixelCenter)
        .getOrElse(airCentroid)
    })
    
    battle.teams.foreach(group =>
      group.vanguard = ByOption
        .minBy(group.units)(_.pixelDistanceCenter(group.opponent.centroid))
        .map(_.pixelCenter)
        .getOrElse(SpecificPoints.middle))
  }
}
