package Placement.Generation

import Information.Geography.Types.Zone
import Lifecycle.With

object Wall {

  def apply(zone: Zone): Option[Fit] = {
    if (With.self.isProtoss) protoss(zone) else if (With.self.isTerran) terran(zone) else zerg(zone)
  }

  def terran(zone: Zone): Option[Fit] = None
  def zerg(zone: Zone): Option[Fit] = None

  def protoss(zone: Zone): Option[Fit] = {
    None
  }
}
