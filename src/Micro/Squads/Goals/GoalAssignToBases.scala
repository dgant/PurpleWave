package Micro.Squads.Goals

import Information.Geography.Types.Base
import Information.Intelligenze.BaseFilterExpansions
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

abstract class GoalAssignToBases extends SquadGoalBasic {

  def takeNextBase(scout: FriendlyUnitInfo): Base

  protected def baseFilter: Base => Boolean = base => base.owner.isNeutral
  protected var destinationByScout: mutable.ArrayBuffer[(FriendlyUnitInfo, Pixel)] = new mutable.ArrayBuffer[(FriendlyUnitInfo, Pixel)]
  protected var destinationFrame: Int = 0
  override def destination: Pixel = {
    destinationByScout
      .headOption
      .map(_._2)
      .filter(_.base.exists(baseFilter))
      .getOrElse(baseToPixel(With.scouting.peekNextBaseToScout(baseFilter)))
  }
  protected def baseToPixel(base: Base): Pixel = base.heart.pixelCenter

  
  override def run() {
    squad.units.foreach(unit => {
      With.scouting.highlightScout(unit)
      val scoutPixel = destinationByScout.find(_._1 == unit).map(_._2).getOrElse(destination)
      unit.agent.intend(squad.client, new Intention {
        toTravel = Some(scoutPixel)
        canFocus = true
      })
    })
  }

  private def scoutPreference(unit: FriendlyUnitInfo): Double = {
    val scoutDestination = baseToPixel(With.scouting.peekNextBaseToScout(BaseFilterExpansions.apply))
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
        * (if (unit.is(Protoss.Zealot) && With.enemies.map(With.unitsShown(_, Terran.Vulture)).sum > 0) 3.0 else 1.0)
      )
    else
      Double.MaxValue
  }
}
