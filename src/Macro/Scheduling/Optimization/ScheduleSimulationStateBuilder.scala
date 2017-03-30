package Macro.Scheduling.Optimization

import ProxyBwapi.Techs.{TechTypes, Techs}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.Upgrades.{UpgradeTypes, Upgrades}
import Startup.With
import Utilities.CountMap

import scala.collection.{breakOut, mutable}

object ScheduleSimulationStateBuilder {
  def build:ScheduleSimulationState = {
    val techsOwned    = TechTypes.all.filter(With.self.hasResearched).map(Techs.get).to[mutable.HashSet]
    val upgradesOwned = UpgradeTypes.all.map(upgrade => (Upgrades.get(upgrade), With.self.getUpgradeLevel(upgrade))).toMap
  
    val output = new ScheduleSimulationState(
      frame           = With.frame,
      minerals        = With.minerals,
      gas             = With.gas,
      supplyAvailable = With.supplyTotal - With.supplyUsed,
      unitsOwned      = unitCount,
      unitsAvailable  = unitCount,
      techsOwned      = techsOwned,
      upgradeLevels   = upgradesOwned.map(identity)(breakOut),
      eventQueue      = ScheduleSimulationEventAnticipator.anticipate.to[mutable.SortedSet])
    
    //This should happen in stable order! The alternative is flickering
    output.eventQueue.foreach(output.assumeEvent)
    output
  }
  
  def unitCount:CountMap[UnitClass] = {
    val output = new CountMap[UnitClass]
    With.units.ours
      .groupBy(_.unitClass)
      .foreach(pair => output.put(pair._1, pair._2.size))
    output
  }
}
