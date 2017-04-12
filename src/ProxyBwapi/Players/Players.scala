package ProxyBwapi.Players

import Lifecycle.With
import bwapi.Player

object Players {
  def all:Iterable[PlayerInfo] = With.proxy.playersByPlayer.values
  def get(player:Player):PlayerInfo = With.proxy.playersByPlayer(player.getID)
}
