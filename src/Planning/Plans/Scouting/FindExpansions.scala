package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobile, UnitMatchNot}
import Planning.Composition.UnitPreferences.UnitPreference
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FindExpansions extends Plan {
  
  description.set("Find enemy expansions")
  
  private val scoutPreference = new UnitPreference {
    override def preference(unit: FriendlyUnitInfo): Double = {
      if (unit.canMove) (
        - unit.topSpeed
        * (if (unit.flying) 1.5 else 1.0)
        * (if (unit.cloaked) 1.5 else 1.0)
        / unit.pixelDistanceCenter(With.intelligence.leastScoutedBases.head.heart.pixel)
      )
      else
        Double.MaxValue
    }
  }
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchAnd(
      UnitMatchMobile,
      UnitMatchNot(Terran.Battlecruiser),
      UnitMatchNot(Terran.Valkyrie),
      UnitMatchNot(Protoss.Arbiter),
      UnitMatchNot(Protoss.Carrier)))
    unitPreference.set(scoutPreference)
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  var lastReleaseFrame = 0
  
  override def onUpdate() {
    if (false && With.framesSince(lastReleaseFrame) > GameTime(0, 15)()) {
      scouts.get.release()
      lastReleaseFrame = With.frame
    }
    scouts.get.unitCounter.set(new UnitCountBetween(1, Math.max(1, With.self.supplyUsed / 80)))
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout: FriendlyUnitInfo) = {
    With.intelligence.highlightScout(scout)
    scout.agent.intend(this, new Intention {
      toTravel = getNextScoutingBase.map(_.heart.pixelCenter)
      canCower = true
    })
  }
  
  private def getNextScoutingBase: Option[Base] = {
    def acceptable(base: Base) = {
      base.owner.isNeutral && ( ! base.zone.island || With.strategy.isPlasma)
    }
    val baseFirst = With.intelligence.dequeueNextBaseToScout
    var baseNext = baseFirst
    do {
      if (acceptable(baseNext)) return Some(baseNext)
      baseNext = With.intelligence.dequeueNextBaseToScout
    } while (baseNext != baseFirst)
    None
  }
}
