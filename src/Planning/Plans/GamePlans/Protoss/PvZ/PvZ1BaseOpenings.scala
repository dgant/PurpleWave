package Planning.Plans.GamePlans.Protoss.PvZ

import Debugging.SimpleString
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Protoss

abstract class PvZ1BaseOpenings extends PvZ1BaseBuildOrders {

  protected trait   Opening       extends SimpleString
  protected object  Open910       extends Opening
  protected object  Open1012      extends Opening
  protected object  OpenZZCoreZ   extends Opening
  protected object  OpenGateNexus extends Opening

  protected var opening: Opening = Open1012

  protected def open(allowExpanding: Boolean): Unit = {
    if (units(Protoss.Gateway) < 2 && units(Protoss.Nexus) < 2 && ! have(Protoss.Assimilator)) {
      if (opening == Open1012 && enemyRecentStrategy(With.fingerprints.fourPool)) {
        opening = Open910
      } else if (With.fingerprints.twelveHatch() && ! With.fingerprints.twoHatchMain() && ! With.fingerprints.twoHatchGas() && allowExpanding) {
        opening = OpenGateNexus
      } else if (With.fingerprints.overpool() || (With.fingerprints.hatchFirst() && ! enemyRecentStrategy(With.fingerprints.tenHatch)) || ! enemyRecentStrategy(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)) {
        opening = OpenZZCoreZ
      }
    }
    opening match {
      case Open910      => open910()
      case Open1012     => open1012()
      case OpenZZCoreZ  => openZZCoreZ()
      case _            => openGateNexus()
    }
    if (bases <= 1 && anticipateSpeedlings) {
      get(3, Protoss.Zealot)
      get(2, Protoss.Gateway)
      get(7, Protoss.Zealot)
      get(Protoss.Forge)
      get(2, Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireLabelYes(PlaceLabels.DefendEntrance))
    }
    scoutOn(Protoss.Pylon)
    With.blackboard.scoutExpansions.set(false)
    status(anticipateSpeedlings, "Speedlings")
    status(opening.toString)
  }
}
