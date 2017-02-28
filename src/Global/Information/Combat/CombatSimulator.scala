package Global.Information.Combat

import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Limiter
import bwapi.UnitType
import Utilities.Enrichment.EnrichUnitType._

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
    val ourGroupMaps = _groupUnits(With.units.ours)
    val enemyGroupMaps = _groupUnits(With.units.enemy)
    val ourGroups = ourGroupMaps.map(group => new CombatGroup(group._1.position, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new CombatGroup(group._1.position, group._2))
    combats = _buildCombats(ourGroups, enemyGroups)
  }
  
  def _simulateCombats() {
    combats.foreach(_update)
  }
  
  def _mapUnitsToNeighbors(units:Iterable[UnitInfo]):Map[UnitInfo, Iterable[UnitInfo]] = {
    //Yes, this includes the unit itself
    units.map(unit => (unit, With.units.inRadius(unit.position, combatRange))).toMap
  }
  
  def _groupUnits(units:Iterable[UnitInfo]):mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] = {
    val neighborsByFighter = _mapUnitsToNeighbors(units)
    val fightersClosestToEnemyFighters = neighborsByFighter.keys
      .filter(_.canFight)
      .toList
      .sortBy(fighter => (List(Double.MaxValue) ++neighborsByFighter(fighter)
        .filter(_.enemyOf(fighter))
        .filter(_.canFight)
        .map(_.position.getDistance(fighter.position)))
        .min)
  
    val leaderBySoldier = new mutable.HashMap[UnitInfo, UnitInfo]
    val groupsByLeader = new mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] {
      override def default(key: UnitInfo):mutable.HashSet[UnitInfo] = {
        put(key, new mutable.HashSet[UnitInfo])
        this(key)}}
  
    fightersClosestToEnemyFighters.foreach(leader => {
      if ( ! leaderBySoldier.contains(leader)) {
        groupsByLeader(leader).add(leader)
        groupsByLeader(leader) ++= neighborsByFighter(leader).filter(_.player == leader.player)
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
  
  def _removeMissingUnits(units:mutable.HashSet[UnitInfo]) {
    units.filterNot(_.alive).foreach(units.remove)
  }
  
  def _valueUnit(unit:UnitInfo, simulation: CombatSimulation):Int = {
    //Don't be afraid to fight workers.
    //This is a hack, because it's affecting our expectation of combat, rather than our motivation for fighting
    if (unit.unitType.isWorker) return 0
    
    //Fails to account for casters
    //Fails to account for carriers/reavers
    //Fails to account for upgrades (including range upgrades)
    
    var dps = unit.groundDps
    var range = unit.range
    
    if (unit.unitType == UnitType.Terran_Bunker) {
      dps = 4 * UnitType.Terran_Marine.groundDps
      range = UnitType.Terran_Marine.range
    }
    
    val distanceFactor = Math.max(0, combatRange + unit.range - unit.position.getApproxDistance(simulation.focalPoint))
    val combatEfficacy = unit.groundDps * unit.totalHealth
    Math.max(0, distanceFactor * combatEfficacy)
  }
}
