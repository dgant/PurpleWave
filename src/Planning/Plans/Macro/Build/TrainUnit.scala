package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.{UnitPreferIdle, UnitPreference}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TrainUnit(val traineeClass: UnitClass) extends ProductionPlan {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(trainerLock)
  override def producerInProgress: Boolean = trainee.isDefined
  override def buildable: Buildable = BuildableUnit(traineeClass)

  description.set("Train a " + traineeClass)
  
  val currencyLock    = new LockCurrencyForUnit(traineeClass)
  val trainerClass    = traineeClass.whatBuilds._1
  val addonsRequired  = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val matchTrainer    = UnitMatchAnd(trainerClass, UnitMatchNot(UnitMatchMobileFlying))
  val trainerMatcher  =
    if (addonsRequired.isDefined)
      UnitMatchAnd(matchTrainer, UnitMatchHasAddon(addonsRequired.head))
    else if (traineeClass == Terran.NuclearMissile)
      UnitMatchAnd(matchTrainer, UnitMatchNot(UnitMatchHasNuke))
    else
      matchTrainer
    
  val trainerLock = new LockUnits {
    unitMatcher.set(trainerMatcher)
    unitCounter.set(UnitCountOne)
    unitPreference.set(UnitPreferIdle)
  }
  
  private var trainer: Option[FriendlyUnitInfo] = None
  private var trainee: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = trainee.exists(_.completeOrNearlyComplete)
  
  override def onUpdate() {
    if (isComplete) return
  
    // Trainee dead? Forget we had one.
    // Have a trainer but no trainee? Check for trainee.
    
    trainee = trainee.filter(theTrainee => trainer.exists(_.trainee.exists(_.is(traineeClass))))
    
    if (trainer.isDefined && trainee.isEmpty) {
      trainee = trainer.flatMap(_.trainee.filter(t => t.is(traineeClass) && t.trainerPlan.forall(_ == this)))
    }
    trainee.foreach(_.setTrainerPlan(this))
  
    // Duplicated across MorphUnit
    currencyLock.framesPreordered = (
      traineeClass.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(trainerClass)
      :+ trainer.map(_.remainingOccupationFrames).getOrElse(0)).max
    currencyLock.isSpent = trainee.isDefined || trainer.exists(_.trainingQueue.headOption.contains(traineeClass))
    currencyLock.acquire(this)
    if (currencyLock.satisfied) {
      updateTrainerPreference()
      trainerLock.acquire(this)
      trainer = trainerLock.units.headOption
      if (trainee.isEmpty) {
        trainer.foreach(_.agent.intend(this, new Intention { toTrain = Some(traineeClass) }))
      }
    }
  }
  private def updateTrainerPreference() {
    val locationPreference = new UnitPreference {
      override def preference(unit: FriendlyUnitInfo): Double = {
        val safetyFramesMin = GameTime(0, 1)()
        val safetyFramesMax = GameTime(0, 10)()
        def measureSafety(frames: () => Double): Double = {
          if (unit.battle.isEmpty)
            safetyFramesMax
          else
            PurpleMath.clamp(frames(), safetyFramesMin, safetyFramesMax)
        }
        val framesToLive    = measureSafety(() => unit.matchups.framesToLive)
        val framesOfSafety  = measureSafety(() => unit.matchups.framesOfSafety)
        val distance        = Math.max(1.0, unit.pixelDistanceCenter(With.intelligence.mostBaselikeEnemyTile.pixelCenter))
        val workers         = Math.max(1.0, if (traineeClass.isWorker) unit.base.map(_.workerCount).sum else 1.0)
        val addons          = if (addonsRequired.isEmpty && traineeClass != Terran.SCV) 1.0 + 10.0 * unit.addon.size else 1.0
        addons * distance * workers / framesToLive / framesOfSafety
      }
    }
    
    trainerLock.unitPreference.set(locationPreference)
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
