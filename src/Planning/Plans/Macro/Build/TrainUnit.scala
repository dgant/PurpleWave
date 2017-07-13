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
import Macro.Scheduling.Project

class TrainUnit(val traineeClass: UnitClass) extends Plan {
  
  description.set("Train a " + traineeClass)
  
  val currencyLock    = new LockCurrencyForUnit(traineeClass)
  val trainerClass    = traineeClass.whatBuilds._1
  val trainerLock     = new LockUnits {
    unitMatcher.set(UnitMatchType(trainerClass))
    unitCounter.set(UnitCountOne)
  }
  
  private var trainer: Option[FriendlyUnitInfo] = None
  private var trainee: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = trainee.exists(_.aliveAndComplete)
  
  def matches(candidateTrainee: FriendlyUnitInfo, candidateTrainer: FriendlyUnitInfo): Boolean = {
    ! candidateTrainee.complete       &&
    candidateTrainee.alive            &&
    candidateTrainee.is(traineeClass) &&
    candidateTrainee.pixelCenter == candidateTrainer.pixelCenter
  }
  
  override def onUpdate() {
    if (isComplete) return
  
    // Trainee dead? Forget we had one.
    // Have a trainer but no trainee? Check for trainee.
    
    trainee = trainee.filter(theTrainee => trainer.exists(theTrainer => matches(theTrainee, theTrainer)))
    
    if (trainer.isDefined && trainee.isEmpty) {
      trainee = With.units.ours.find(unit => matches(unit, trainer.get))
    }
  
    currencyLock.framesAhead = (
      traineeClass.buildUnitsEnabling.map(enablingClass => Project.framesToUnits(enablingClass, 1))
      :+ Project.framesToUnits(trainerClass, 1)).max
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
