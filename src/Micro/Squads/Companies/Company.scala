package Micro.Squads.Companies

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Company {
  
  var units: Seq[FriendlyUnitInfo] = Seq.empty
  
  def allowed(unitClass: UnitClass): Boolean
}
