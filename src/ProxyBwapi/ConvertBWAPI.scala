package ProxyBwapi

import Mathematics.Points.Pixel
import ProxyBwapi.Techs.Tech
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Position, TechType, UpgradeType}

object ConvertBWAPI {
  private val badPositions: Vector[Position] = Vector(Position.Invalid, Position.None, Position.Unknown, null)
  def position(position: Position): Option[Pixel] = {
    if (badPositions.contains(position)) None else Some(new Pixel(position))
  }

  private val badTechs = Vector(TechType.None, TechType.Unknown, null)
  def tech(tech: TechType): Option[Tech] = if (badTechs.contains(tech)) None else Some(Tech(tech))

  private val badUpgrades = Vector(UpgradeType.None, UpgradeType.Unknown, null)
  def upgrade(upgrade: UpgradeType): Option[Upgrade] = if (badUpgrades.contains(upgrade)) None else Some(Upgrade(upgrade))
}
