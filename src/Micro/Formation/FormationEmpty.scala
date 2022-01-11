package Micro.Formation
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FormationEmpty extends Formation {
  def style: FormationStyle = FormationStyleEmpty
  def placements: Map[FriendlyUnitInfo, Pixel] = Map.empty
}

