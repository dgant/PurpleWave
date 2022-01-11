package Micro.Formation

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class FormationBasic(style: FormationStyle, placements: Map[FriendlyUnitInfo, Pixel]) extends Formation
