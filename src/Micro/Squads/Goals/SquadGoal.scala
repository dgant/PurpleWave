package Micro.Squads.Goals

import Micro.Squads.RecruitmentLevel.RecruitmentLevel
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait SquadGoal {
  var squad: Squad = _
  
  def run()
  def offer(candidates: Iterable[FriendlyUnitInfo], recruitmentNeed: RecruitmentLevel): Iterable[FriendlyUnitInfo]
  
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}
