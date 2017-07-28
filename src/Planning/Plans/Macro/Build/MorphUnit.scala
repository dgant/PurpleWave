package Planning.Plans.Macro.Build

import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MorphUnit(val classToMorph: UnitClass) extends Plan {
  
  description.set("Morph a " + classToMorph)
  
  val currencyLock    = new LockCurrencyForUnit(classToMorph)
  val morpherClass    = classToMorph.whatBuilds._1
  val morpherLock     = new LockUnits {
    unitMatcher.set(UnitMatchType(morpherClass))
    unitCounter.set(UnitCountOne)
  }
  
  private var morpher: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = morpher.filter(_.unitClass == classToMorph).exists(_.aliveAndComplete)
  
  override def onUpdate() {
    
    if (isComplete) return
  
    // Duplicated across TrainUnit
    currencyLock.framesAhead = (
      classToMorph.buildUnitsEnabling.map(enablingClass => Project.framesToUnits(enablingClass, 1))
      :+ Project.framesToUnits(morpherClass, 1)).max
    
    currencyLock.isSpent = morpher.exists(m => m.alive && m.unitClass != morpherClass)
    currencyLock.acquire(this)
    
    if (currencyLock.satisfied && ! currencyLock.isSpent) {
      morpherLock.acquire(this)
      morpher = morpherLock.units.headOption
      morpher.foreach(_.intend(new Intention(this) { toTrain = Some(classToMorph) }))
    }
  }
}
