package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.{Buildable, BuildableTech, BuildableUnit, BuildableUpgrade}
import Types.UnitInfo.FriendlyUnitInfo

object ScheduleSimulationEventAnticipator {
  
  //BIG TODO:
  //We need IMPLICIT to tell us whether to add to owned units or just available units
  
  def anticipate:Iterable[SimulationEvent] = {
    With.units.ours.flatten(unit => {
      List(
        getUnitCompletion(unit, unit.framesBeforeBecomingComplete),
        getUnitCompletion(unit, unit.framesBeforeBuildeeComplete, implicitEvent = true),
        getUnitCompletion(unit, unit.framesBeforeTechComplete,    implicitEvent = true),
        getUnitCompletion(unit, unit.framesBeforeUpgradeComplete, implicitEvent = true),
        getTechCompletion(unit),
        getUpgradeCompletion(unit)
      ).flatten
    })
  }
  
  def getUnitCompletion(unit:FriendlyUnitInfo, timeLeft:Int, implicitEvent:Boolean = false): Iterable[SimulationEvent] = {
    if (timeLeft <= 0) return List.empty
    List(buildEvent(new BuildableUnit(unit.utype), timeLeft))
  }
  
  def getTechCompletion(unit:FriendlyUnitInfo): Iterable[SimulationEvent] = {
    val timeLeft = unit.framesBeforeTechComplete
    if (timeLeft <= 0) return List.empty
    List(buildEvent(new BuildableTech(unit.teching), timeLeft))
  }
  
  def getUpgradeCompletion(unit:FriendlyUnitInfo): Iterable[SimulationEvent] = {
    val timeLeft = unit.framesBeforeUpgradeComplete
    if (timeLeft <= 0) return List.empty
    val upgrade = unit.upgrading
    val level = With.game.self.getUpgradeLevel(upgrade)
    List(buildEvent(new BuildableUpgrade(upgrade, level), timeLeft))
  }
  
  def buildEvent(buildable:Buildable, framesLeft:Int, implicitEvent:Boolean = false):SimulationEvent =
    new SimulationEvent(
      buildable,
      frameStart(buildable.frames, framesLeft),
      frameEnd(buildable.frames, framesLeft),
      isImplicit = implicitEvent)
  
  def frameStart(framesTotal:Int, framesLeft:Int):Int = With.game.getFrameCount + framesLeft - framesTotal
  def frameEnd  (framesTotal:Int, framesLeft:Int):Int = With.game.getFrameCount + framesLeft
}
