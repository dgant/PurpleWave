package Tactics.Production

import Macro.Buildables.Buildable
import Macro.Scheduling.MacroCounter
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitPreferences.PreferTrainerFor
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TrainUnit(val buildableUnit: Buildable) extends Production {
  setBuildable(buildableUnit)
  val traineeClass    : UnitClass         = buildable.unit.get
  val trainerClass    : UnitClass         = traineeClass.whatBuilds._1
  val addonClass      : Option[UnitClass] = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val currencyLock    : LockCurrency      = new LockCurrencyFor(this, traineeClass, 1)
  val trainerLock     : LockUnits         = new LockUnits(this)
  trainerLock.counter     = CountOne
  trainerLock.preference  = PreferTrainerFor(traineeClass)
  trainerLock.matcher     = u => (
    trainerClass(u)
    && ! u.hasNuke
    && ! u.flying
    && addonClass.forall(u.addon.contains)
    && (trainer.contains(u) || u.friendly.exists(_.trainee.forall(t => traineeClass(t) || t.completeOrNearlyComplete))))
  
  def trainer: Option[FriendlyUnitInfo] = trainerLock.units.headOption
  def trainee: Option[FriendlyUnitInfo] = trainer.flatMap(_.trainee.filter(traineeClass))
  override def isComplete: Boolean = trainee.exists(MacroCounter.countComplete(_)(traineeClass) > 0)
  override def hasSpent: Boolean = trainee.isDefined
  
  override def onUpdate() {
    if (isComplete) return
    trainerLock.acquire()
    trainee.foreach(_.setProducer(this))
    if (hasSpent) return
    if (currencyLock.acquire()) {
      if (trainer.exists(_.remainingOccupationFrames > 0)) {
        trainerLock.reacquire()
      }
      trainer.foreach(_.intend(this, new Intention { toTrain = Some(traineeClass) }))
    }
  }
}
