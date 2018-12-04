package Mathematics.Formations.Designers

import Information.Geography.Types.Base
import Mathematics.Formations.FormationAssigned
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FormationBase(base: Base) extends FormationDesigner {

  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    base.zone.formation.form(units)
  }
}
