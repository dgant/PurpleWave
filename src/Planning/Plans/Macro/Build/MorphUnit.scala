package Planning.Plans.Macro.Build

import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.{UnitMatchMorphingInto, UnitMatchOr}
import Planning.Composition.UnitPreferences.{UnitPreferBaseWithFewerWorkers, UnitPreferHatcheryWithMoreLarva}
import Planning.Plan
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MorphUnit(val classToMorph: UnitClass) extends Plan {
  
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
      classToMorph.buildUnitsEnabling.map(Project.framesToUnits(_, 1))
      :+ Project.framesToUnits(morpherClass, 1)).max
    
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
      morpherLock.unitPreference.set(UnitPreferBaseWithFewerWorkers)
    } else if (morpherClass == Zerg.Larva) {
      morpherLock.unitPreference.set(UnitPreferHatcheryWithMoreLarva)
    } else {
      // AIST1 hack fix: Disabling this so we stop morphing all our Hatcheries into Lairs
      //morpherLock.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
    }
  }
}
