package Tactic.Production

import Information.Counting.MacroCounter
import Macro.Requests.{RequestBuildable, RequestUnit}
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.{PreferTiers, PreferTrainerFor}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.util.Try

class TrainUnit(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val traineeClass    : UnitClass           = request.unit.get
  val trainerClass    : UnitClass           = traineeClass.whatBuilds._1
  val addonClass      : Option[UnitClass]   = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val currencyLock    : LockCurrency        = new LockCurrencyFor(this, traineeClass, 1)
  val trainerLock     : LockUnits           = new LockUnits(this)
  val requestUnit     : Option[RequestUnit] = Try(requestArg.asInstanceOf[RequestUnit]).toOption
  trainerLock.counter     = CountOne
  trainerLock.preference  = requestUnit.flatMap(_.parentPreference).map(p => PreferTiers(p, PreferTrainerFor(traineeClass))).getOrElse(PreferTrainerFor(traineeClass))
  trainerLock.matcher     = u => trainerClass(u) && ! u.hasNuke && ! u.flying && addonClass.forall(u.addon.contains) && requestUnit.forall(_.parentRequirement.forall(r => u.friendly.forall(r)))

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
      trainer.foreach(_.intend(this, new Intention))
      return
    }
    if (currencyLock.acquire()) {
      if (trainer.exists(_.remainingOccupationFrames > 0)) {
        trainerLock.reacquire()
      }
      trainer.filter(_.trainingQueue.size < 2).foreach(_.intend(this, new Intention { toTrain = Some(traineeClass) }))
    }
  }
}
