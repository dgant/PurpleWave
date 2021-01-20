package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scan extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toScan.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.useTechOnPixel(unit, Terran.ScannerSweep, unit.agent.toScan.get)
    unit.agent.toScan = None
  }
}
