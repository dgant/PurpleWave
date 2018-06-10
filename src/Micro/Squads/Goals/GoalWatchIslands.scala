package Micro.Squads.Goals

import Information.Geography.Types.Base
import Lifecycle.With
import Performance.Cache
import ProxyBwapi.Races.Zerg

class GoalWatchIslands extends GoalAssignToBases {
  
  override def toString: String = "Watch island bases"
  
  override def acceptsHelp: Boolean = true
  
  private def likelyBasesCache = new Cache(() => {
    With.geography.neutralBases.filter(_.zone.island).toVector.sortBy(_.heart.tileDistanceFast(With.geography.home))
      .headOption.toVector // Temporary until we can fix the waffling assignment issue
  })
  
  unitMatcher = Zerg.Overlord
  
  override def peekNextBase: Base = takeNextBase
  override def takeNextBase: Base = likelyBasesCache()(squad.units.size % likelyBasesCache().size)
}
