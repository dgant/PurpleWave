package ProxyBwapi.Upgrades

import bwapi.UpgradeType

case class Upgrade(base:UpgradeType) {

  val levels          = (1 to base.maxRepeats).toList
  val mineralPrice    = levels.map(i => (i, base.mineralPrice(i))).toMap
  val gasPrice        = levels.map(i => (i, base.gasPrice(i))).toMap
  val upgradeTime     = levels.map(i => (i, base.upgradeTime(i))).toMap
  
  /*
    Not implemented:
    
    id (not available via BWMirror)
    race
    gasPriceFactor
    mineralPriceFactor
    upgradeTimeFactor
    whatsrequred
    whatUpgrades
    whatUses
   */
}
