package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.Types.{Battle, BattleGlobal, BattleLocal, Team}
import Lifecycle.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

class BattleClassifier {
    
  var global      : BattleGlobal                = _
  var byUnit      : Map[UnitInfo, BattleLocal]  = Map.empty
  var local       : Vector[BattleLocal]         = Vector.empty
  var lastUpdate  : Int                         = 0
  
  def all: Traversable[Battle] = local :+ global
  
  val clustering = new BattleClustering
  
  def run() {
    clustering.enqueue(With.units.all.filter(isEligibleLocal))
    clustering.run()
    replaceBattleGlobal()
    replaceBattlesLocal()
    BattleUpdater.run()
    
    // If we have the time to do so, run lazy estimations here.
    // Otherwise, it'll run on demand from the Micro task
    if (With.performance.continueRunning) {
      global.globalSafeToAttack
      local.foreach(_.estimationSimulationAttack)
      local.foreach(_.estimationSimulationRetreat)
      local.foreach(_.netEngageValue)
    }
    
    lastUpdate = With.frame
  }
  
  private def isEligibleLocal(unit: UnitInfo): Boolean = {
    isEligible(unit) && unit.likelyStillThere
  }
  
  private def isEligibleGlobal(unit: UnitInfo): Boolean = {
    isEligible(unit)
  }
  
  private def isEligible(unit: UnitInfo): Boolean = {
    unit.likelyStillAlive && (unit.complete || unit.unitClass.isBuilding)
  }
  
  private def replaceBattleGlobal() {
    global = new BattleGlobal(
      new Team(asVectorUs     (With.units.ours  .filter(isEligibleGlobal))),
      new Team(asVectorEnemy  (With.units.enemy .filter(isEligibleGlobal))))
  }
  
  private def replaceBattlesLocal() {
    local = clustering.clusters
      .map(cluster =>
        new BattleLocal(
          new Team(cluster.filter(_.isOurs).toVector),
          new Team(cluster.filter(_.isEnemy).toVector)))
      .filter(_.teams.forall(_.units.exists(_.canAttack)))
    
    byUnit = local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
  }
  
  private def asVectorUs    (units: Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def asVectorEnemy (units: Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
}
