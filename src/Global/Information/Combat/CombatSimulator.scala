package Global.Information.Combat

import Startup.With
import Utilities.Enrichment.EnrichUnit.EnrichedUnit
import Utilities.Limiter

import scala.collection.JavaConverters._
import scala.collection.mutable

class CombatSimulator {
  
  val combatRange = 32 * 15
  var combats:Iterable[CombatSimulation] = List.empty
  
  val limitCombatIdentification = new Limiter(24, _defineCombats)
  val limitCombatSimulation = new Limiter(4, _simulateCombats)
  def onFrame() {
    limitCombatIdentification.act()
    limitCombatSimulation.act()
  }
  
  def _defineCombats() {
    val ourGroupMaps = _groupUnits(With.ourUnits)
    val enemyGroupMaps = _groupUnits(With.enemyUnits)
    val ourGroups = ourGroupMaps.map(group => new CombatGroup(group._1.getPosition, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new CombatGroup(group._1.getPosition, group._2))
    combats = _buildCombats(ourGroups, enemyGroups)
  }
  
  def _simulateCombats() {
    combats.foreach(_update)
  }
  
  def _mapUnitsToNeighbors(units:Iterable[bwapi.Unit]):Map[bwapi.Unit, Iterable[bwapi.Unit]] = {
    //Yes, this includes the unit itself
    units.map(unit => (unit, unit.getUnitsInRadius(combatRange).asScala)).toMap
  }
  
  def _groupUnits(units:Iterable[bwapi.Unit]):mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] = {
    val neighborsByFighter = _mapUnitsToNeighbors(units)
    val fightersClosestToEnemyFighters = neighborsByFighter.keys
      .filter(_.canFight)
      .toList
      .sortBy(fighter => (List(Int.MaxValue) ++neighborsByFighter(fighter)
        .filter(_.isEnemyOf(fighter))
        .filter(_.canFight)
        .map(_.getDistance(fighter)))
        .min)
  
    val leaderBySoldier = new mutable.HashMap[bwapi.Unit, bwapi.Unit]
    val groupsByLeader = new mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] {
      override def default(key: bwapi.Unit):mutable.HashSet[bwapi.Unit] = {
        put(key, new mutable.HashSet[bwapi.Unit])
        this(key)}}
  
    fightersClosestToEnemyFighters.foreach(leader => {
      if ( ! leaderBySoldier.contains(leader)) {
        groupsByLeader(leader).add(leader)
        groupsByLeader(leader) ++= neighborsByFighter(leader).filter(_.getPlayer == leader.getPlayer)
        groupsByLeader(leader).foreach(groupMember => leaderBySoldier.put(groupMember, leader))
      }})
    
    return groupsByLeader
  }
  
  def _buildCombats(
    ourGroups:  Iterable[CombatGroup],
    theirGroups:Iterable[CombatGroup]):Iterable[CombatSimulation] = {
    //Shouldn't happen, but just in case
    if (theirGroups.isEmpty) return List.empty
  
    ourGroups.map(ourGroup =>
      new CombatSimulation(ourGroup, theirGroups.minBy(_.vanguard.getDistance(ourGroup.vanguard))))
  }
  
  def _update(simulation: CombatSimulation) {
    _removeMissingUnits(simulation.ourGroup.units)
    _removeMissingUnits(simulation.enemyGroup.units)
    simulation.ourScore = simulation.ourGroup.units.map(_valueUnit(_, simulation)).sum
    simulation.enemyScore = simulation.enemyGroup.units.map(_valueUnit(_, simulation)).sum
  }
  
  def _removeMissingUnits(units:mutable.HashSet[bwapi.Unit]) {
    units.filterNot(unit => With.unit(unit.getID).nonEmpty).foreach(units.remove)
  }
  
  def _valueUnit(unit:bwapi.Unit, simulation: CombatSimulation):Int = {
    //Don't be afraid to fight workers.
    //This is a hack, because it's affecting our expectation of combat, rather than our motivation for fighting
    if (unit.getType.isWorker) return 0
    
    //Fails to account for bunkers
    //Fails to account for casters
    //Fails to account for carriers/reavers
    
    val distanceFactor = Math.max(0, combatRange + unit.range - unit.getPosition.getApproxDistance(simulation.focalPoint))
    val combatEfficacy = unit.groundDps * unit.totalHealth
    Math.max(0, distanceFactor * combatEfficacy)
  }
}
