package Global.Resources.Scheduling

import Startup.With
import Types.BwapiTypes.{TechTypes, UpgradeTypes}
import bwapi.{UnitType, UpgradeType}

import scala.collection.{breakOut, mutable}

object ScheduleSimulationStateBuilder {
  def build:ScheduleSimulationState = {
    val techsOwned      = TechTypes.all.filter(With.game.self.hasResearched).to[mutable.HashSet]
    val upgradesOwned   = UpgradeTypes.all.map(upgrade => (upgrade, With.game.self.getUpgradeLevel(upgrade))).toMap
    val upgradesOwnedMutable: mutable.Map[UpgradeType, Int] = upgradesOwned.map(identity)(breakOut)
  
    val output = new ScheduleSimulationState(
      frame           = With.frame,
      minerals        = With.game.self.minerals,
      gas             = With.game.self.gas,
      supplyAvailable = With.game.self.supplyTotal - With.game.self.supplyUsed,
      unitsOwned      = unitCount,
      unitsAvailable  = unitCount,
      techsOwned      = techsOwned,
      upgradeLevels   = upgradesOwnedMutable,
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
