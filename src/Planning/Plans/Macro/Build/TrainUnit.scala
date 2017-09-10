package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchHasAddon}
import Planning.Composition.UnitPreferences.UnitPreferNoAddon
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TrainUnit(val traineeClass: UnitClass) extends Plan {
  
  description.set("Train a " + traineeClass)
  
  val currencyLock    = new LockCurrencyForUnit(traineeClass)
  val trainerClass    = traineeClass.whatBuilds._1
  val addonsRequired  = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val matchTrainer    = trainerClass
  val trainerMatcher  =
    if (addonsRequired.isDefined)
      UnitMatchAnd(matchTrainer, UnitMatchHasAddon(addonsRequired.head))
    else
      matchTrainer
    
  val trainerLock = new LockUnits {
    unitMatcher.set(trainerMatcher)
    unitCounter.set(UnitCountOne)
  }
  
  if (addonsRequired.isEmpty) {
    trainerLock.unitPreference.set(UnitPreferNoAddon)
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
  
    // Duplicated across MorphUnit
    currencyLock.framesPreordered = (
      traineeClass.buildUnitsEnabling.map(enablingClass => Project.framesToUnits(enablingClass, 1))
      :+ Project.framesToUnits(trainerClass, 1)).max
    currencyLock.isSpent = trainee.isDefined || trainer.exists(_.trainingQueue.headOption.contains(traineeClass))
    currencyLock.acquire(this)
    if (currencyLock.satisfied) {
      trainerLock.acquire(this)
      trainer = trainerLock.units.headOption
      if (trainee.isEmpty) {
        trainer.foreach(_.agent.intend(this, new Intention { toTrain = Some(traineeClass) }))
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
