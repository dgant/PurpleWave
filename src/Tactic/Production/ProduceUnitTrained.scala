package Tactic.Production

import Information.Counting.MacroCounter
import Macro.Requests.{RequestBuildable, RequestUnit}
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.{PreferTiers, PreferTrainerFor}

import scala.util.Try

class ProduceUnitTrained(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val traineeClass    : UnitClass           = request.unit.get
  val trainerClass    : UnitClass           = traineeClass.whatBuilds._1
  val addonClass      : Option[UnitClass]   = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val requestUnit     : Option[RequestUnit] = Try(requestArg.asInstanceOf[RequestUnit]).toOption
  val currencyLock    : LockCurrency        = new LockCurrencyFor(this, traineeClass)
  val trainerLock     : LockUnits           = new LockUnits(this,
    (u: UnitInfo)  => trainerClass(u) && ! u.hasNuke && ! u.flying && addonClass.forall(u.addon.contains) && requestUnit.forall(_.parentRequirement.forall(r => u.friendly.forall(r))),
    requestUnit.flatMap(_.parentPreference).map(p => PreferTiers(p, PreferTrainerFor(traineeClass))).getOrElse(PreferTrainerFor(traineeClass)),
    CountOne)

  var finalTrainer: Option[FriendlyUnitInfo] = None
  var finalTrainee: Option[FriendlyUnitInfo] = None
  def trainer: Option[FriendlyUnitInfo] = finalTrainer.orElse(trainerLock.units.headOption)
  override def trainee: Option[FriendlyUnitInfo] = finalTrainee.orElse(trainer.flatMap(_.trainee.filter(traineeClass).filter(_.producer.forall(==))))
  override def hasSpent: Boolean = trainee.isDefined
  override def isComplete: Boolean = trainee.exists(MacroCounter.countComplete(_)(traineeClass) > 0)
  override def expectTrainee(unit: FriendlyUnitInfo): Boolean = {
    if (trainer.exists(t => traineeClass(unit) && t.pixel == unit.pixel)) {
      finalTrainee = Some(unit)
      true
    } else false
  }
  override def onUpdate(): Unit = {
    // As we approach completion, lock in our trainer/trainee for our records and let another task acquire it
    if (trainee.exists(t => t.alive && t.remainingCompletionFrames < 96)) {
      finalTrainer = trainer
      finalTrainee = trainee
      trainerLock.release()
      return
    }
    finalTrainer = None
    finalTrainee = None
    trainerLock.acquire()
    trainee.foreach(_.setProducer(this))
    if (hasSpent) {
      trainer.foreach(_.intend(this)) // Do nothing, mainly; possibly cancel if needed
      return
    }
    if (currencyLock.acquire()) {
      if (trainer.exists(_.remainingOccupationFrames > 0)) {
        trainerLock.reacquire()
      }
      trainer.filter(_.trainingQueue.size < 2).foreach(_.intend(this).setTrain(traineeClass))
    }
  }
}
