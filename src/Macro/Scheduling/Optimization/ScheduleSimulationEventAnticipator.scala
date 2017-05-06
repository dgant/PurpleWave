package Macro.Scheduling.Optimization

import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Macro.Buildables.{Buildable, BuildableTech, BuildableUnit, BuildableUpgrade}
import Macro.Scheduling.BuildEvent
import Lifecycle.With

object ScheduleSimulationEventAnticipator {
  
  def anticipate:Iterable[BuildEvent] = {
    With.units.ours.toVector.flatten(unit => {
      Vector(
        getUnitCompletion(unit, unit.framesBeforeBecomingComplete),
        getTechCompletion(unit),
        getUpgradeCompletion(unit)
      ).flatten
    })
  }
  
  def getUnitCompletion(unit:FriendlyUnitInfo, timeLeft:Int):
      Iterable[BuildEvent] = {
    if (timeLeft <= 0) return Vector.empty
    Vector(buildEvent(new BuildableUnit(unit.unitClass), timeLeft))
  }
  
  def getTechCompletion(unit:FriendlyUnitInfo): Iterable[BuildEvent] = {
    val timeLeft = unit.framesBeforeTechComplete
    if (timeLeft <= 0) return Vector.empty
    Vector(buildEvent(new BuildableTech(unit.techingType), timeLeft))
  }
  
  def getUpgradeCompletion(unit:FriendlyUnitInfo): Iterable[BuildEvent] = {
    val timeLeft = unit.framesBeforeUpgradeComplete
    if (timeLeft <= 0) return Vector.empty
    val upgrade = unit.upgradingType
    val level = 1 + With.self.getUpgradeLevel(upgrade)
    Vector(buildEvent(new BuildableUpgrade(upgrade, level), timeLeft))
  }
  
  def buildEvent(buildable:Buildable, framesLeft:Int):BuildEvent =
    new BuildEvent(buildable, -1, With.frame + framesLeft)
}
