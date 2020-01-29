package Mathematics.Formations.Designers

import Mathematics.Formations.FormationAssigned
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait FormationDesigner {
  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned
}
