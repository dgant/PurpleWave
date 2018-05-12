package Micro.Squads.Goals

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobile, UnitMatchNot}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class GoalFindExpansions extends GoalBasic {
  
  override def run() {
    squad.units.foreach(orderScout)
  }
  
  override def acceptsHelp: Boolean = squad.units.size < PurpleMath.clamp(With.self.supplyUsed / 80, 1, With.geography.neutralBases.size)
  
  override def sortAndFilterCandidates(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    val output = new mutable.PriorityQueue[FriendlyUnitInfo]()(Ordering.by(scoutPreference))
    candidates.foreach(u => if (u.is(
        UnitMatchAnd(
          UnitMatchMobile,
          UnitMatchNot(Terran.Battlecruiser),
          UnitMatchNot(Terran.Valkyrie),
          UnitMatchNot(Protoss.Arbiter),
          UnitMatchNot(Protoss.Carrier))))
      output += u)
    output
  }
  
  private def scoutPreference(unit: FriendlyUnitInfo): Double = {
    if (unit.canMove) (
      - unit.topSpeed
        * (if (unit.flying) 1.5 else 1.0)
        * (if (unit.cloaked) 1.5 else 1.0)
        / unit.pixelDistanceCenter(With.intelligence.leastScoutedBases.head.heart.pixel)
      )
    else
      Double.MaxValue
  }
  
  private def orderScout(scout: FriendlyUnitInfo) = {
    With.intelligence.highlightScout(scout)
    scout.agent.intend(squad.client, new Intention {
      toTravel = getNextScoutingBase.map(_.heart.pixelCenter)
    })
  }
  
  private def getNextScoutingBase: Option[Base] = {
    def acceptable(base: Base) = base.owner.isNeutral && ( ! base.zone.island || With.strategy.isPlasma)
    val baseFirst = With.intelligence.dequeueNextBaseToScout
    var baseNext = baseFirst
    do {
      if (acceptable(baseNext)) return Some(baseNext)
      baseNext = With.intelligence.dequeueNextBaseToScout
    } while (baseNext != baseFirst)
    None
  }
}
