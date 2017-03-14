package Global.Resources.Scheduling

import Startup.With
import Types.BwapiTypes.{TechTypes, UpgradeTypes}
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
  
  def unitCount:collection.mutable.HashMap[UnitType, Int] = {
    collection.mutable.HashMap(
      With.units.ours
        .groupBy(_.utype)
        .map(pair => (pair._1, pair._2.size)).toSeq: _*)
  }
}
