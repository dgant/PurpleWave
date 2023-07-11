package ProxyBwapi

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.Techs.Tech
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Position, TechType, UpgradeType}

object ConvertBWAPI {
  def angle(bwapiAngle: Double): Double = {
    // Ankmairdor: "zero angle is north, and it goes clockwise"
    //
    // The units are radians, converted from [-128, 128]:
    // https://github.com/bwapi/bwapi/blob/3438abd8e0222f37934ba62b2130c3933b067678/bwapi/BWAPI/Source/BWAPI/UnitUpdate.cpp#L212

    Maff.normalize0To2Pi(bwapiAngle + Maff.halfPi)
  }

  def position(position: Position): Option[Pixel] = {
    // Comparing to x/y coordinates of Position.[Invalid, None, Unknown]
    if (position == null || position.x >= 32000 || position.y >= 32000) None else Some(new Pixel(position))
  }

  private val badTechs = Vector(TechType.None, TechType.Unknown, null)
  def tech(tech: TechType): Option[Tech] = if (badTechs.contains(tech)) None else Some(Tech(tech))

  private val badUpgrades = Vector(UpgradeType.None, UpgradeType.Unknown, null)
  def upgrade(upgrade: UpgradeType): Option[Upgrade] = if (badUpgrades.contains(upgrade)) None else Some(Upgrade(upgrade))
}
