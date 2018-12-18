package Micro.Squads.Goals

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

abstract class GoalAssignToBases extends GoalBasic {
  
  def peekNextBase: Base
  def takeNextBase(scout: FriendlyUnitInfo): Base

  protected def baseFilter: Base => Boolean = base => base.owner.isNeutral
  protected var destinationByScout: mutable.ArrayBuffer[(FriendlyUnitInfo, Pixel)] = new mutable.ArrayBuffer[(FriendlyUnitInfo, Pixel)]
  protected var destinationFrame: Int = 0
  override protected def destination: Pixel = {
    destinationByScout.headOption.map(_._2).getOrElse(baseToPixel(With.intelligence.peekNextBaseToScout(baseFilter)))
  }
  protected def baseToPixel(base: Base): Pixel = base.heart.pixelCenter
  
  override def prepareForCandidates() {
    destinationByScout.clear()
  }
  
  override def run() {
    squad.units.foreach(unit => {
      With.intelligence.highlightScout(unit)
      val scoutPixel = destinationByScout.find(_._1 == unit).map(_._2).getOrElse(destination)
      unit.agent.intend(squad.client, new Intention {
        toTravel = Some(scoutPixel)
        canFocus = true
      })
    })
  }
  
  override protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]): Unit = {}
  override protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]): Unit = {
    if ( ! acceptsHelp) return
    var remainingCandidates = candidates
    var foundCandidate: Option[FriendlyUnitInfo] = None
    do {
      foundCandidate = ByOption.minBy(filterCandidates(candidates))(scoutPreference)
      foundCandidate.foreach(newScout => {
        addCandidate(newScout)
        destinationByScout.append((newScout, baseToPixel(takeNextBase(newScout))))
      })
    } while(foundCandidate.nonEmpty && acceptsHelp)
  }
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]): Unit = {}
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]): Unit = {}
  
  private def scoutPreference(unit: FriendlyUnitInfo): Double = {
    val scoutDestination = baseToPixel(peekNextBase)
    val typeMultiplier = if (unit.isAny(
      Terran.Battlecruiser,
      Terran.Dropship,
      Terran.Valkyrie,
      Protoss.Arbiter,
      Protoss.Archon,
      Protoss.Carrier,
      Protoss.Shuttle,
      Zerg.Devourer,
      Zerg.Guardian))
      10.0 else 1.0
    
    if (unit.canMove) (
      unit.framesToTravelTo(scoutDestination)
        * typeMultiplier
        * (if (unit.flying) 1.0 else 1.5)
        * (if (unit.cloaked) 1.0 else 1.5)
        * (if (unit.is(Protoss.Zealot) && With.enemies.map(With.intelligence.unitsShown(_, Terran.Vulture)).sum > 0) 3.0 else 1.0)
      )
    else
      Double.MaxValue
  }
}
