package Information.Geography.Types

import Mathematics.Points.Tile
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

trait Geo {
  def units     : Seq[UnitInfo]
  def owner     : PlayerInfo
  def heart     : Tile
  def bases     : Seq[Base]
  def zones     : Seq[Zone]
  def isOurs    : Boolean       = owner.isUs
  def isAlly    : Boolean       = owner.isAlly
  def isEnemy   : Boolean       = owner.isEnemy
  def isNeutral : Boolean       = owner.isNeutral
  def ourUnits  : Seq[UnitInfo] = units.view.filter(_.isOurs)
  def allies    : Seq[UnitInfo] = units.view.filter(_.isFriendly)
  def enemies   : Seq[UnitInfo] = units.view.filter(_.isEnemy)
}
