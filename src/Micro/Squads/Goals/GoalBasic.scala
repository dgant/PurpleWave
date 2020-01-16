package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait GoalBasic extends SquadGoal {

  /////////////////////////////
  // Default implementations //
  /////////////////////////////
  
  override def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
    }))
  }
  
  protected def addCandidate(unit: FriendlyUnitInfo) {
    if (unit.squad.isDefined) return
    squad.recruit(unit)
  }
  
  //////////////////
  // Subclass API //
  //////////////////
  
  var unitMatcher: UnitMatcher = UnitMatchRecruitableForCombat
  var unitCounter: UnitCounter = UnitCountEverything
  protected def acceptsHelp: Boolean = unitCounter.continue(squad.units)
  protected def destination: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
}
