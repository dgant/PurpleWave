package Planning.Plans.Macro.Build

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With

class TrainUnit(val traineeClass:UnitClass) extends Plan {
  
  description.set("Train a " + traineeClass)
  
  val currencyLock = new LockCurrencyForUnit(traineeClass)
  val trainerLock = new LockUnits {
    unitMatcher.set(new UnitMatchType(traineeClass.whatBuilds._1))
    unitCounter.set(UnitCountOne)
  }
  
  private var trainer:Option[FriendlyUnitInfo] = None
  private var trainee:Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = trainee.exists(p => p.alive && p.complete)
  
  override def onFrame() {
    if (isComplete) return
  
    // Trainee dead? Forget we had one.
    // Have a trainer but no trainee? Check for trainee.
    
    trainee = trainee.filter(_.alive)
    
    if (trainer.isDefined && trainee.isEmpty) {
      trainee = With.units.ours
        .filter(unit =>
          ! unit.complete
            && unit.unitClass == traineeClass
            && unit.x == trainer.get.x
            && unit.y == trainer.get.y)
        .headOption
    }
  
    currencyLock.isSpent = trainee.isDefined || trainer.exists(_.trainingQueue.headOption.exists(_ == traineeClass))
    currencyLock.acquire(this)
    if (currencyLock.isComplete) {
      trainerLock.acquire(this)
      trainer = trainerLock.units.headOption
      if (trainee.isEmpty && trainer.isDefined) {
        With.executor.intend(new Intention(this, trainer.get) { toBuild = Some(traineeClass) })
      }
    }
  }
  
  override def drawOverlay() {
    if (isComplete) return
    if (trainer.isEmpty) return
    DrawMap.box(
      trainer.get.tileArea.startPixel,
      trainer.get.tileArea.endPixel,
      DrawMap.playerColorDark(With.self))
    DrawMap.label(
      description.get,
      trainer.get.pixelCenter,
      drawBackground = true,
      DrawMap.playerColorDark(With.self))
  }
}
