package Macro.Scheduling.Optimization

import ProxyBwapi.BwapiTypes.{TechTypes, UpgradeTypes}
import Startup.With
import Utilities.CountMap
import bwapi.UnitType

import scala.collection.{breakOut, mutable}

object ScheduleSimulationStateBuilder {
  def build:ScheduleSimulationState = {
    val techsOwned    = TechTypes.all.filter(With.self.hasResearched).to[mutable.HashSet]
    val upgradesOwned = UpgradeTypes.all.map(upgrade => (upgrade, With.self.getUpgradeLevel(upgrade))).toMap
  
    val output = new ScheduleSimulationState(
      frame           = With.frame,
      minerals        = With.self.minerals,
      gas             = With.self.gas,
      supplyAvailable = With.self.supplyTotal - With.self.supplyUsed,
      unitsOwned      = unitCount,
      unitsAvailable  = unitCount,
      techsOwned      = techsOwned,
      upgradeLevels   = upgradesOwned.map(identity)(breakOut),
      eventQueue      = ScheduleSimulationEventAnticipator.anticipate.to[mutable.SortedSet])
    
    //This should happen in stable order! The alternative is flickering
    output.eventQueue.foreach(output.assumeEvent)
    output
  }
  
  def unitCount:CountMap[UnitType] = {
    val output = new CountMap[UnitType]
    With.units.ours
      .groupBy(_.utype)
      .foreach(pair => output.put(pair._1, pair._2.size))
    output
  }
}
