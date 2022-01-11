package Micro.Formation

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Formation {
  def style: FormationStyle
  def placements: Map[FriendlyUnitInfo, Pixel]
}