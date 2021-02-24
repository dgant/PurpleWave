package Micro.Squads.Recruitment

import Micro.Squads.Goals.SettableSquad
import Micro.Squads.SquadBatch
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait SquadRecruiter extends SettableSquad {
  def inherentValue: Double
  def addCandidate(candidate: FriendlyUnitInfo): Unit
  def candidates: Seq[FriendlyUnitInfo]
  def candidateWelcome(batch: SquadBatch, candidate: FriendlyUnitInfo): Boolean
  def candidateValue(batch: SquadBatch, candidate: FriendlyUnitInfo): Double
}
