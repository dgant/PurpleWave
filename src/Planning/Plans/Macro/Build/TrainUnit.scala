package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Micro.Intent.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Lifecycle.With

class TrainUnit(val traineeClass:UnitClass) extends Plan {
  
  description.set("Train a " + traineeClass)
  
  val currencyLock = new LockCurrencyForUnit(traineeClass)
  val trainerLock = new LockUnits {
    unitMatcher.set(UnitMatchType(traineeClass.whatBuilds._1))
    unitCounter.set(UnitCountOne)
  }
  
  private var trainer:Option[FriendlyUnitInfo] = None
  private var trainee:Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = trainee.exists(_.aliveAndComplete)
  
  override def onUpdate() {
    if (isComplete) return
  
    // Trainee dead? Forget we had one.
    // Have a trainer but no trainee? Check for trainee.
    
    trainee = trainee.filter(_.alive)
    
    if (trainer.isDefined && trainee.isEmpty) {
      trainee = With.units.ours
        .find(unit =>
          ! unit.complete
            && unit.is(traineeClass)
            && unit.x == trainer.get.x
            && unit.y == trainer.get.y)
    }
  
    currencyLock.isSpent = trainee.isDefined || trainer.exists(_.trainingQueue.headOption.contains(traineeClass))
    currencyLock.acquire(this)
    if (currencyLock.satisfied) {
      trainerLock.acquire(this)
      trainer = trainerLock.units.headOption
      if (trainee.isEmpty && trainer.isDefined) {
        With.executor.intend(new Intention(this, trainer.get) { toTrain = Some(traineeClass) })
      }
    }
  }
  
  override def visualize() {
    if (isComplete) return
    if (trainer.isEmpty) return
    DrawMap.box(
      trainer.get.tileArea.startPixel,
      trainer.get.tileArea.endPixel,
      With.self.colorDark)
    DrawMap.label(
      description.get,
      trainer.get.pixelCenter,
      drawBackground = true,
      With.self.colorDark)
  }
}
