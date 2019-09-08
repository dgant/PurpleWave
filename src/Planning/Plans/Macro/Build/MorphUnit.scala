package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchMorphingInto, UnitMatchOr}
import Planning.UnitPreferences.{UnitPreferAll, UnitPreferBaseWithFewerWorkers, UnitPreferBaseWithMoreWorkers, UnitPreferHatcheryWithThreeLarva}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MorphUnit(val classToMorph: UnitClass) extends ProductionPlan {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(morpherLock)
  override def producerInProgress: Boolean = morpher.exists(_.morphing)
  override def buildable: Buildable = BuildableUnit(classToMorph)

  description.set("Morph a " + classToMorph)
  
  val currencyLock    = new LockCurrencyForUnit(classToMorph)
  val morpherClass    = classToMorph.whatBuilds._1
  val morpherLock     = new LockUnits {
    unitMatcher.set(UnitMatchOr(morpherClass, UnitMatchMorphingInto(classToMorph)))
    unitCounter.set(UnitCountOne)
  }
  
  private var morpher: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = morpher.filter(_.unitClass == classToMorph).exists(_.aliveAndComplete)
  
  override def onUpdate() {
    
    if (isComplete) return
  
    // Duplicated across TrainUnit
    currencyLock.framesPreordered = (
      classToMorph.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(morpherClass)).max
    
    currencyLock.isSpent = morpher.exists(m => m.alive && m.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon, classToMorph))
    currencyLock.acquire(this)
    
    if (currencyLock.satisfied && ! currencyLock.isSpent) {
      setPreference()
      morpherLock.acquire(this)
      morpher = morpherLock.units.headOption
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
      // AIST1 hack fix: Disabling this so we stop morphing all our Hatcheries into Lairs
      //morpherLock.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
    }
  }
}
