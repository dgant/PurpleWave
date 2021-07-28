package Information.Geography.Types

import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Player

trait Geo {
  def name: String
  def metro: Metro
  def player: Player
  def isIsland: Boolean
  def tiles: Seq[Tile]
  def tilesBuildable: Seq[Tile]
  def heart: Tile
  def centroidAir: Tile
  def centroidGround: Tile
  def units: Seq[UnitInfo]
  def boundary: Seq[Tile]
  def edges: Seq[Edge]
  def exit: Option[Edge]
}
