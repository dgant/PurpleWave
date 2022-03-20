package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Macro.Scheduling.MacroCounter
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferTrainerFor
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TrainUnit(val buildableUnit: Buildable) extends Production {
  setBuildable(buildableUnit)
  val traineeClass    : UnitClass         = buildable.unit.get
  val trainerClass    : UnitClass         = traineeClass.whatBuilds._1
  val addonClass      : Option[UnitClass] = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val matchTrainer    : MatchAnd          = MatchAnd(trainerClass, MatchNot(MatchMobileFlying))
  val currencyLock    : LockCurrency      = new LockCurrencyFor(this, traineeClass, 1)
  val trainerLock     : LockUnits         = new LockUnits(this)
  trainerLock.counter     = CountOne
  trainerLock.preference  = PreferTrainerFor(traineeClass)
  trainerLock.matcher     = candidate => (
    trainerClass(candidate)
    && addonClass.forall(candidate.addon.contains)
    && ! candidate.hasNuke
    && (trainer.contains(candidate) || candidate.friendly.exists(_.trainee.forall(t => t.is(traineeClass) || t.completeOrNearlyComplete))))
  
  def trainer: Option[FriendlyUnitInfo] = trainerLock.units.headOption
  def trainee: Option[FriendlyUnitInfo] = trainer.flatMap(_.trainee.filter(traineeClass))
  override def isComplete: Boolean = trainee.exists(MacroCounter.countComplete(_)(traineeClass) > 0)
  override def hasSpent: Boolean = trainee.isDefined
  
  override def onUpdate() {
    if (isComplete) return
    if (400 - With.self.supplyUsed < traineeClass.supplyRequired) return

    trainee.foreach(_.setProducer(this))
  
    // Duplicated across MorphUnit
    currencyLock.framesPreordered = (
      traineeClass.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(trainerClass)
      :+ trainer.map(_.remainingOccupationFrames).getOrElse(0)).max
    if (hasSpent || currencyLock.acquire()) {
      trainerLock.acquire()
      if (trainee.isEmpty && trainer.forall(_.buildUnit.exists( ! _.completeOrNearlyComplete))) {
        // If this trainer is occupied right now, release it,
        // because maybe we can get a free trainer next time
        trainerLock.release()
      }
      if (trainee.isEmpty) {
        trainer.foreach(_.intend(this, new Intention { toTrain = Some(traineeClass) }))
      }
    }
  }
}
