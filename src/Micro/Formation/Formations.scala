package Micro.Formation

import Mathematics.Points.Pixel
import Tactic.Squads.FriendlyUnitGroup

object Formations {
  def march(group: FriendlyUnitGroup, destination: Pixel) : Formation = {
    new FormationStandard(group, FormationStyleMarch, destination)
  }

  def guard(group: FriendlyUnitGroup, toGuard: Option[Pixel]): Formation = {
    val guardZone = toGuard.getOrElse(group.homeConsensus).zone
    guardZone.exitOriginal
      .map(exit => FormationZone(group, guardZone, exit))
      .getOrElse(new FormationStandard(group, FormationStyleGuard, group.homeConsensus))
  }

  def engage(group: FriendlyUnitGroup, towards: Pixel): Formation = {
    new FormationStandard(group, FormationStyleMarch, towards)
  }

  def disengage(group: FriendlyUnitGroup, towards: Option[Pixel] = None): Formation = {
    new FormationStandard(group, FormationStyleDisengage, towards.getOrElse(group.homeConsensus))
  }
}
