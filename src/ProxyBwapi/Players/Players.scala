package ProxyBwapi.Players

import Lifecycle.With
import bwapi.Player

object Players {
  def all: Iterable[PlayerInfo] = With.proxy.playersById.values
  def get(player: Player): PlayerInfo = With.proxy.playersById(player.getID)
}
