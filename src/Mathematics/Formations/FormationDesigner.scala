package Mathematics.Formations

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait FormationDesigner {
  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned
}
