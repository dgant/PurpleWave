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
import Planning.UnitPreferences.UnitPreference
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
  }
  
  private var trainer: Option[FriendlyUnitInfo] = None
  private var trainee: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = trainee.exists(_.completeOrNearlyComplete)

  protected def assignTrainerAndTrainee(): Unit = {
    trainer = trainerLock.units.headOption
    trainee = trainer.flatMap(_.trainee.filter(_.is(traineeClass)))
  }
  
  override def onUpdate() {
    if (isComplete) return
  
    assignTrainerAndTrainee()
  
    // Duplicated across MorphUnit
    currencyLock.framesPreordered = (
      traineeClass.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(trainerClass)
      :+ trainer.map(_.remainingOccupationFrames).getOrElse(0)).max
    currencyLock.isSpent = trainee.isDefined
    currencyLock.acquire(this)
    if (currencyLock.satisfied) {
      updateTrainerPreference()
      trainerLock.acquire(this)
      assignTrainerAndTrainee()
      // If this trainer is occupied right now, release it,
      // because maybe we can get a free trainer next time
      if (trainer.exists(_.buildUnit.exists(u => u.unitClass != traineeClass && ! u.completeOrNearlyComplete))) {
        trainerLock.release()
        trainer = None
      }
      if (trainee.isEmpty) {
        trainer.foreach(_.agent.intend(this, new Intention { toTrain = Some(traineeClass) }))
      }
    }
  }

  val safetyFramesMax = GameTime(0, 10)()
  lazy val mapSize = With.mapTileWidth + With.mapTileHeight
  private def updateTrainerPreference() {
    val locationPreference = new UnitPreference {
      override def preference(trainer: FriendlyUnitInfo): Double = {
        // Factors, ranging on [0, 1]
        val addon     = if (trainer.addon.isDefined) 0.0 else 1.0
        val readiness = trainer.remainingOccupationFrames.toDouble / Math.max(trainer.trainee.map(_.unitClass.buildFrames).getOrElse(0), traineeClass.buildFrames)
        val workers   = if (traineeClass.isWorker) PurpleMath.clamp(trainer.base.map(_.saturation()).getOrElse(0.0), 0.0, 1.0) else 0.0
        val health    = trainer.totalHealth / trainer.unitClass.maxTotalHealth.toDouble
        val safety    = PurpleMath.clamp(trainer.matchups.framesOfSafety, 0, safetyFramesMax) / safetyFramesMax.toDouble
        val distance  = 1.0 - PurpleMath.clamp(trainer.tileIncludingCenter.groundPixels(With.intelligence.mostBaselikeEnemyTile) / mapSize, 0.0, 1.0)
        val score = (
            10000000000.0 * addon
          + 100000000.0   * workers
          + 1000000.0     * readiness
          + 10000.0       * health
          + 100.0         * safety
          + 1.0           * distance
        )
        score
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
