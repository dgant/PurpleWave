package ProxyBwapi

import Mathematics.Points.Pixel
import ProxyBwapi.Techs.Tech
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Position, TechType, UpgradeType}

object ConvertBWAPI {
  def position(position: Position): Option[Pixel] = {
    // Comparing to x/y coordinates of Position.[Invalid, None, Unknown]
    if (position == null || position.x >= 32000 || position.y >= 32000) None else Some(new Pixel(position))
  }

  private val badTechs = Vector(TechType.None, TechType.Unknown, null)
  def tech(tech: TechType): Option[Tech] = if (badTechs.contains(tech)) None else Some(Tech(tech))

  private val badUpgrades = Vector(UpgradeType.None, UpgradeType.Unknown, null)
  def upgrade(upgrade: UpgradeType): Option[Upgrade] = if (badUpgrades.contains(upgrade)) None else Some(Upgrade(upgrade))
}
