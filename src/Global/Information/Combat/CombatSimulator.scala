package Global.Information.Combat

import Startup.With
import Utilities.Enrichment.EnrichUnit.EnrichedUnit

import scala.collection.JavaConverters._
import scala.collection.mutable

class CombatSimulator {
  
  val combatRange = 32 * 15
  var combats:Iterable[CombatSimulation] = List.empty
  
  def onFrame() {
    
    //Assign to groups based on proximity to combat
    //Identify line of engagement
    //Simulate combat in tug-of-war tournament
    //
    //Warning: Ignores fogged units!
    
    val ourGroupMaps = _groupUnits(With.ourUnits)
    val enemyGroupMaps = _groupUnits(With.enemyUnits)
    val ourGroups = ourGroupMaps.map(group => new CombatGroup(group._1.getPosition, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new CombatGroup(group._1.getPosition, group._2))
    combats = _predictCombats(ourGroups, enemyGroups)
  }
  
  def _mapUnitsToNeighbors(units:Iterable[bwapi.Unit]):Map[bwapi.Unit, Iterable[bwapi.Unit]] = {
    units.map(x => (x, x.getUnitsInRadius(combatRange).asScala)).toMap
  }
  
  def _groupUnits(units:Iterable[bwapi.Unit]):mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] = {
    val neighborsByFighter = _mapUnitsToNeighbors(With.ourUnits.filter(_.canFight))
    val fightersClosestToEnemyFighters = neighborsByFighter.keys
      .toList
      .sortBy(fighter => (List(Int.MaxValue) ++neighborsByFighter(fighter)
        .filter(_.isEnemyOf(fighter))
        .filter(_.canAttack)
        .map(_.getDistance(fighter)))
        .min)
  
    val leaderBySoldier = new mutable.HashMap[bwapi.Unit, bwapi.Unit]
    val groupsByLeader = new mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] {
      override def default(key: bwapi.Unit):mutable.HashSet[bwapi.Unit] = new mutable.HashSet[bwapi.Unit] }
  
    fightersClosestToEnemyFighters.foreach(leader => {
      if ( ! leaderBySoldier.contains(leader)) {
        leaderBySoldier.put(leader, leader)
        groupsByLeader(leader) ++= neighborsByFighter(leader)
        neighborsByFighter(leader).foreach(neighbor => leaderBySoldier.put(neighbor, leader))
      }})
    
    return groupsByLeader
  }
  
  def _predictCombats(
    ourGroups:  Iterable[CombatGroup],
    theirGroups:Iterable[CombatGroup]):Iterable[CombatSimulation] = {
    //Shouldn't happen, but just in case
    if (theirGroups.isEmpty) return List.empty
  
    val combats = ourGroups.map(ourGroup =>
      new CombatSimulation(ourGroup, theirGroups.minBy(_.vanguard.getDistance(ourGroup.vanguard))))
    
    combats.foreach(_predictCombat)
    
    combats
  }
  
  def _predictCombat(simulation: CombatSimulation) {
    simulation.ourScore = simulation.ourGroup.units.map(_valueUnit(_, simulation)).sum
    simulation.enemyScore = simulation.ourGroup.units.map(_valueUnit(_, simulation)).sum
  }
  
  def _valueUnit(unit:bwapi.Unit, simulation: CombatSimulation):Int = {
    val distanceFactor = combatRange + unit.range - unit.getPosition.getApproxDistance(simulation.focalPoint)
    val combatEfficacy = unit.groundDps * unit.totalHealth / unit.initialTotalHealth
    Math.max(0, distanceFactor * combatEfficacy)
  }
}
