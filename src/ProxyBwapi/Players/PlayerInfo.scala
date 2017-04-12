package ProxyBwapi.Players

import Debugging.Visualization.Colors
import bwapi.Player

case class PlayerInfo(basePlayer:Player) extends PlayerProxy(basePlayer) {
  
  def isFriendly = isUs || isAlly
  
  lazy val colorDark: bwapi.Color   =
    if      (isUs)      Colors.DarkViolet
    else if (isNeutral) Colors.DarkGray
    else if (isAlly)    Colors.DarkBlue
    else                Colors.DarkRed
  
  lazy val colorNeon: bwapi.Color   =
    if      (isUs)      Colors.NeonViolet
    else if (isNeutral) Colors.NeonTeal
    else if (isAlly)    Colors.NeonBlue
    else                Colors.NeonRed
}
