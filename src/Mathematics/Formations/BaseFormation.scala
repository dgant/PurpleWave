package Mathematics.Formations

import Information.Geography.Types.Base
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class BaseFormation(base: Base) extends FormationDesigner {

  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    base.zone.formation.form(units)
  }
}
