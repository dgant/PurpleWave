package Macro.Buildables

import ProxyBwapi.Upgrades.Upgrade

case class BuildableUpgrade(upgrade:Upgrade, level: Int=1) extends Buildable {
  override def upgradeOption  : Option[Upgrade] = Some(upgrade)
  override def upgradeLevel   : Int             = level
  override def toString       : String          = upgrade.toString + " " + upgradeLevel
  override def frames         : Int             = upgrade.upgradeFrames(upgradeLevel)
}
