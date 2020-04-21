package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Macro.Scheduling.MacroCounter
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchMorphingInto, UnitMatchOr, UnitMatchSpecific}
import Planning.UnitPreferences._
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MorphUnit(val classToMorph: UnitClass) extends ProductionPlan {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(morpherLock)
  override def producerInProgress: Boolean = morpher.exists(_.morphing)
  override def buildable: Buildable = BuildableUnit(classToMorph)

  description.set("Morph a " + classToMorph)
  
  val currencyLock  = new LockCurrencyForUnit(classToMorph)
  val morpherClass  = classToMorph.whatBuilds._1
  val morpherLock   = new LockUnits {
    unitMatcher.set(UnitMatchOr(morpherClass, UnitMatchMorphingInto(classToMorph)))
    unitCounter.set(UnitCountOne)
  }
  
  private var morpher: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = morpher.exists(t => MacroCounter.countComplete(t)(classToMorph) > 0)
  
  override def onUpdate() {
  
    // Duplicated across TrainUnit
    currencyLock.framesPreordered = (
      classToMorph.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(morpherClass)).max
    
    currencyLock.isSpent = morpher.exists(t => MacroCounter.countCompleteOrIncomplete(t)(classToMorph) > 0)
    currencyLock.acquire(this)
    
    if (currencyLock.satisfied && ! currencyLock.isSpent) {
      setPreference()
      morpherLock.unitMatcher.set(morpher.map(m => new UnitMatchSpecific(Set(m))).getOrElse(morpherClass))
      morpherLock.acquire(this)
      morpher = morpherLock.units.headOption
      morpher.foreach(_.setProducer(this))
      morpher.foreach(_.agent.intend(this, new Intention {
        toTrain = Some(classToMorph)
        canFlee = classToMorph == Zerg.Lurker
        canAttack = classToMorph != Zerg.Lurker
      }))
    }
  }
  
  protected def setPreference() {
    if (classToMorph.isWorker) {
      morpherLock.unitPreference.set(UnitPreferAll(
        UnitPreferHatcheryWithThreeLarva,
        UnitPreferBaseWithFewerWorkers
      ))
    } else if (morpherClass == Zerg.Larva) {
      morpherLock.unitPreference.set(UnitPreferAll(
        UnitPreferHatcheryWithThreeLarva,
        UnitPreferBaseWithMoreWorkers
      ))
    } else {
      morpherLock.unitPreference.set(u => (if (u.visibleToOpponents) 0 else 1) - u.matchups.framesOfSafety)
    }
  }
}
