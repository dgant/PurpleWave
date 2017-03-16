package ProxyBwapi.Upgrades

import Performance.Caching.CacheForever
import bwapi.UpgradeType

object Upgrades {
  def get(upgrade: UpgradeType):Upgrade = mapping.get(upgrade)
  def all:Iterable[Upgrade] = mapping.get.values
  
  private val mapping = new CacheForever[Map[UpgradeType, Upgrade]](() => UpgradeTypes.all.map(UpgradeType => (UpgradeType, new Upgrade(UpgradeType))).toMap)
}

