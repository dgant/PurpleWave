package ProxyBwapi.Upgrades

import bwapi.UpgradeType

object Upgrades {
  val get:Map[UpgradeType, Upgrade] = UpgradeTypes.all.map(techType => (techType, new Upgrade(techType))).toMap
  val all = get.values.toList
}
