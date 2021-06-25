package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scan extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.intent.toScan.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.useTechOnPixel(unit, Terran.ScannerSweep, unit.intent.toScan.get)
    // Ensure we don't spam scan in the absence of a new intention
    unit.intent.toScan = None
  }
}
